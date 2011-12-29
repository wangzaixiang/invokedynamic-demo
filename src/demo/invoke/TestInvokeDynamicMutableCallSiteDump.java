package demo.invoke;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestInvokeDynamicMutableCallSiteDump implements Opcodes {

	public static void main(String[] args) throws Exception {
		
		String dir = "gen";
		String path = "demo/invoke/TestInvokeDynamicMutableCallSite.class";
		byte[] clazz = dump();
		
		File f = new File(new File(dir), path);
		if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
			throw new IOException("Cannot create directory "
					+ f.getParentFile());
		}
		FileOutputStream o = new FileOutputStream(f);
		o.write(clazz);
		o.close();

	}
	
	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		MethodVisitor mv;

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER,
				"demo/invoke/TestInvokeDynamicMutableCallSite", null,
				"java/lang/Object", null);


		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
//			Handle constantBSM = new Handle(
//					H_INVOKESTATIC,
//					"invokedynamic/BootstrapUtils",
//					"simpleCall",
//					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/CallSite;");

			Handle mutableBSM = new Handle(H_INVOKESTATIC, "invokedynamic/BootstrapUtils", "mutableCallSite", 
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");

			
			mv = cw.visitMethod(ACC_STATIC, "testInvokeDynamic", "()V", null,
					new String[] { "java/lang/Throwable" });
			mv.visitCode();

			mv.visitLdcInsn("Hello World");
			mv.visitVarInsn(ASTORE, 0);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
					"currentTimeMillis", "()J");
			mv.visitVarInsn(LSTORE, 1);

			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 3);
			

			Label l4 = new Label();
			mv.visitJumpInsn(GOTO, l4);
			Label l5 = new Label();
			mv.visitLabel(l5);
			
			mv.visitVarInsn(ALOAD, 0);
			mv.visitIntInsn(BIPUSH, 111);
			mv.visitIntInsn(BIPUSH, 79);
			
			// change from InvokeVirtual to InvokeDynamic
			mv.visitInvokeDynamicInsn("replace", "(Ljava/lang/String;CC)Ljava/lang/String;", mutableBSM); 
//			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace",
//					"(CC)Ljava/lang/String;");
			
			mv.visitVarInsn(ASTORE, 4);
			

			mv.visitIincInsn(3, 1);
			mv.visitLabel(l4);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitLdcInsn(new Integer(TestSimple.LOOPS));
			mv.visitJumpInsn(IF_ICMPLT, l5);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
					"currentTimeMillis", "()J");
			mv.visitVarInsn(LSTORE, 3);

			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("TestInvokeDynamic with mutable callsite Total time is ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(LLOAD, 3);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LSUB);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"append", "(J)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(Ljava/lang/String;)V");

			mv.visitInsn(RETURN);

			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
