package invokedynamic.agent;

import invokedynamic.InvokeDynamicUtils;
import invokedynamic.InvokeDynamicUtils.InvokeDynamicBootstrap;
import invokedynamic.agent.EvaluationInterepter.DumpMethodHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;

import demo.invoke.TestInvokeDynamicConstantCallSite_plain;

public class InvokeDynamicTransfer {

	/**
	 * 
	 * <pre>
	 * &#064;InvokeDynamicBootstrap(type = BootstrapUtils.class, name = &quot;simpleCall&quot;, args = Class.class)
	 * private final static MethodHandle BSM = BootStrap(String.class);
	 * </pre>
	 */
	static class BootStrapMethod {

		String methodHandleName;

		Class<?> containerClass;

		String methodName;

		Class[] argTypes;

		/**
		 * bsmArgs the bootstrap method constant arguments. Each argument must
		 * be an {@link Integer}, {@link Float}, {@link Long}, {@link Double},
		 * {@link String}, {@link Type} or {@link Handle} value.
		 */
		Object[] args;

		FieldInsnNode putStatic;

		Handle handle;

		public Handle getHandle() {
			if (handle == null) {
				Type[] args = new Type[3 + argTypes.length];
				args[0] = Type.getType(MethodHandles.Lookup.class);
				args[1] = Type.getType(String.class);
				args[2] = Type.getType(MethodType.class);

				for (int i = 3; i < args.length; i++) {
					args[i] = Type.getType(argTypes[i - 3]);
				}

				Type argsType = Type.getMethodType(
						Type.getType(CallSite.class), args);

				handle = new Handle(Opcodes.H_INVOKESTATIC,
						Type.getInternalName(containerClass), methodName,
						argsType.getInternalName());
			}
			return handle;
		}
	}

	List<BootStrapMethod> bootStrapMethods = new ArrayList<>();

	private ClassNode classNode;

	private MethodNode clinit;
	private Frame<TypeAndValue>[] clinitFrames;

	public InvokeDynamicTransfer(ClassNode classNode) {
		this.classNode = classNode;
	}


	byte[] transfer() throws Exception {

		findBSM(classNode);

		replaceInvokeDynamic();

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}


	void findBSM(ClassNode node) throws ClassNotFoundException,
			AnalyzerException {

		for (FieldNode field : node.fields) {
			if (field.visibleAnnotations != null) {
				for (AnnotationNode an : field.visibleAnnotations) {
					if (an.desc
							.equals(classDesc(InvokeDynamicUtils.InvokeDynamicBootstrap.class))) {

						BootStrapMethod bsm = new BootStrapMethod();
						bsm.methodHandleName = field.name;

						for (int i = 0; i < an.values.size(); i = i + 2) {
							String name = (String) an.values.get(i);
							Object value = an.values.get(i + 1);
							switch (name) {
							case "type":
								Type type = (Type) value;
								bsm.containerClass = Class.forName(type
										.getClassName());
								break;
							case "name":
								bsm.methodName = (String) value;
								break;
							case "args":
								List<Type> types = (List<Type>) value;
								bsm.argTypes = new Class[types.size()];
								for (int j = 0; j < types.size(); j++) {
									bsm.argTypes[j] = Class.forName(types
											.get(j).getClassName());
								}
							}
						}

						bsm.getHandle();
						bootStrapMethods.add(bsm);
					}
				}
			}
		}

		parseBootStrapMethodArgs();
		validBootStrapMethodArgs();
		removePutStaticInstructions();
		removeBSMFields();

	}

	private void removeBSMFields() {
		List<FieldNode> remove = new ArrayList<>();
		for(FieldNode field: classNode.fields) {
			String name = field.name;
			if(findBootStrapMethodHandle(name) != null){
				remove.add(field);
			}
		}
		classNode.fields.removeAll(remove);
	}

	/*
	 * mv.visitFieldInsn(GETSTATIC,
	 * "demo/invoke/TestInvokeDynamicConstantCallSite_plain", "BSM",
	 * "Ljava/lang/invoke/MethodHandle;"); mv.visitVarInsn(ALOAD, 0);
	 * mv.visitIntInsn(BIPUSH, 111); mv.visitIntInsn(BIPUSH, 79);
	 * mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle",
	 * "invokeExact", "(Ljava/lang/String;CC)Ljava/lang/String;");
	 */
	void replaceInvokeDynamic() {
		for (MethodNode method : classNode.methods) {
			if (method.instructions != null)
				replaceInvokeDynamic(method);
		}
	}

	private void replaceInvokeDynamic(MethodNode method) {

		boolean changed = false;
		do {
			changed = false;
			for (AbstractInsnNode insn : method.instructions.toArray()) {
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode invoke = (MethodInsnNode) insn;
					if (invoke.getOpcode() == Opcodes.INVOKEVIRTUAL
							&& invoke.owner
									.equals("java/lang/invoke/MethodHandle")
							&& invoke.name.equals("invokeExact")) {
						Type[] argsType = Type.getArgumentTypes(invoke.desc);
						AbstractInsnNode prev = invoke;
						for (int i = 0; i <= argsType.length; i++) {
							prev = prev.getPrevious();
						}
						// check it is GETSTATIC
						if (prev.getOpcode() == Opcodes.GETSTATIC
								&& prev.getNext().getOpcode() == Opcodes.LDC) {
							FieldInsnNode getStatic = (FieldInsnNode) prev;
							LdcInsnNode ldc = (LdcInsnNode) prev.getNext();

							if (getStatic.owner.equals(classNode.name)
									&& findBootStrapMethodHandle(getStatic.name) != null) {
								replaceInvokeDynamic(method, getStatic, ldc,
										invoke);
								changed = true;
								break;	// continue next loop
							}

						}
					}
				}
			}
		} while (changed);

	}

	private void replaceInvokeDynamic(MethodNode method,
			FieldInsnNode getStatic, LdcInsnNode ldc, MethodInsnNode invoke) {

		String methodName = (String) ldc.cst;

		Type[] argTypes = Type.getArgumentTypes(invoke.desc);
		Type[] newArgTypes = new Type[argTypes.length - 1];
		System.arraycopy(argTypes, 1, newArgTypes, 0, newArgTypes.length);

		Type returnType = Type.getReturnType(invoke.desc);

		BootStrapMethod handle = findBootStrapMethodHandle(getStatic.name);

		Type dynamicDesc = Type.getMethodType(returnType, newArgTypes);

		InvokeDynamicInsnNode invokeDynamic = new InvokeDynamicInsnNode(
				methodName, dynamicDesc.getInternalName(), handle.handle,
				handle.args);

		AbstractInsnNode preInvoke = invoke.getPrevious();
		method.instructions.remove(getStatic);
		method.instructions.remove(ldc);

		method.instructions.remove(invoke);
		method.instructions.insert(preInvoke, invokeDynamic);

	}

	private BootStrapMethod findBootStrapMethodHandle(String name) {
		for (BootStrapMethod bsm : bootStrapMethods) {
			if (bsm.methodHandleName.equals(name))
				return bsm;
		}
		return null;
	}

	private void removePutStaticInstructions() {
		List<Integer> remove = new ArrayList<>();

		AbstractInsnNode[] insnNodes = clinit.instructions.toArray();
		Frame<TypeAndValue>[] clinitFrames = this.clinitFrames;

		for (BootStrapMethod bsm : bootStrapMethods) {
			removePutStaticInstruction(bsm.putStatic, insnNodes, clinitFrames,
					remove);
		}

		Collections.sort(remove, Collections.reverseOrder());
		for (Integer index : remove) {
			clinit.instructions.remove(insnNodes[index]);
		}
	}

	private void removePutStaticInstruction(FieldInsnNode putStatic,
			AbstractInsnNode[] insnNodes, Frame<TypeAndValue>[] frames,
			List<Integer> remove) {

		int index;
		for (index = 0; index < insnNodes.length
				&& insnNodes[index] != putStatic; index++) {
		}
		;
		for (int i = index; i >= 0; i--) {
			remove.add(i);
			if (frames[i].getStackSize() == 0)
				break;
		}

	}

	private void validBootStrapMethodArgs() {
		for (BootStrapMethod bsm : bootStrapMethods) {

		}
	}

	/**
	 * parse the <clinit> method to fill args for each BSM handle
	 * 
	 * @throws AnalyzerException
	 */
	private void parseBootStrapMethodArgs() throws AnalyzerException {

		clinit = findMethodNode(classNode, "<clinit>");

		EvaluationInterepter eval = new EvaluationInterepter(this);
		Analyzer<TypeAndValue> a = new Analyzer<>(eval);

		clinitFrames = a.analyze(classNode.name, clinit);

	}

	private MethodNode findMethodNode(ClassNode clazz, String name) {

		for (MethodNode node : clazz.methods) {
			if (node.name.equals(name))
				return node;
		}
		return null;
	}

	private String classDesc(Class<?> it) {
		if (Object.class.isAssignableFrom(it)) {
			return "L" + it.getName().replace(".", "/") + ";";
		}
		throw new UnsupportedOperationException();
	}

	public void checkBootStrapMethodArgs(FieldInsnNode putStatic,
			TypeAndValue value) {
		for (BootStrapMethod bsm : bootStrapMethods) {
			if (putStatic.name.equals(bsm.methodHandleName)
					&& putStatic.owner.equals(classNode.name)
					&& putStatic.desc.equals("Ljava/lang/invoke/MethodHandle;")) {
				if (value != null && value.hasValue()) {
					DumpMethodHandle args = (DumpMethodHandle) value.value();

					Object bsmArgs[] = new Object[args.args.length];
					for (int i = 0; i < bsmArgs.length; i++) {
						Object origin = args.args[i];
						if (origin instanceof Class) {
							bsmArgs[i] = Type.getType((Class) origin);
						}
					}

					bsm.args = bsmArgs;
					bsm.putStatic = putStatic;
				}
			}
		}

	}

	public static void main(String[] args) throws Exception {

		ClassReader reader = new ClassReader(
				TestInvokeDynamicConstantCallSite_plain.class.getName());

		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		byte[] result = new InvokeDynamicTransfer(node).transfer();

		CheckClassAdapter.verify(new ClassReader(result), true, new PrintWriter(System.out));

		FileOutputStream out = new FileOutputStream("gen/demo/invoke/TestInvokeDynamicConstantCallSite_plain.class");
		out.write(result);
		out.close();

		
	}

}
