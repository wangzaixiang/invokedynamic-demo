package invokedynamic;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

public class GenerateInvokeDynamicClass {

	public static void main(String[] args) throws IOException {
		new GenerateInvokeDynamicClass().generateTestPrintClass();
	}

	protected void generate(final String dir, final String path,
			final byte[] clazz) throws IOException {
		File f = new File(new File(dir), path);
		if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
			throw new IOException("Cannot create directory "
					+ f.getParentFile());
		}
		FileOutputStream o = new FileOutputStream(f);
		o.write(clazz);
		o.close();
	}

	void generateTestPrintClass() throws IOException {

		generate("gen", "invokedynamic/TestPrint.class", dumpTestPrint());

	}

	private byte[] dumpTestPrint() {

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		cw.visit(V1_7, ACC_PUBLIC, "invokedynamic/TestPrint", null,
				"java/lang/Object", null);

		{

			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V",
					null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);

			mv.visitEnd();

		}

		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main",
					"([Ljava/lang/String;)V", null, null);
			mv.visitCode();

			Handle constantBSM = new Handle(
					H_INVOKESTATIC,
					"invokedynamic/BootstrapUtils",
					"simpleCall",
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/CallSite;");

			Handle mutableBSM = new Handle(H_INVOKESTATIC, "invokedynamic/BootstrapUtils", "mutableCallSite", 
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
			
			mv.visitTypeInsn(NEW, "invokedynamic/Base");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "invokedynamic/Base", "<init>",
					"()V");
			mv.visitVarInsn(ASTORE, 1);
			
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("Hello World by invokevirtual");
			mv.visitMethodInsn(INVOKEVIRTUAL, "invokedynamic/Base", "println", "(Ljava/lang/String;)V");
			
			// ConstantCallSite
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitLdcInsn("Hello World");
//			mv.visitInvokeDynamicInsn("println", "(Ljava/lang/Object;Ljava/lang/String;)V", constantBSM,
//					Type.getObjectType("invokedynamic/Base"));

			// MutableCallSite
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn("Hello World by invokedynamic");
			mv.visitInvokeDynamicInsn("println", "(Ljava/lang/Object;Ljava/lang/String;)V", mutableBSM, new Object[0]);

			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);	// required
			mv.visitEnd();

		}

		cw.visitEnd();
		byte[] result = cw.toByteArray();
//		CheckClassAdapter.verify(new ClassReader(result), true, new PrintWriter(System.out));	
		return result;

	}

}
