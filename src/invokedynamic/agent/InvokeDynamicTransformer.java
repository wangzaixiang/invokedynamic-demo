package invokedynamic.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class InvokeDynamicTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		ClassReader reader = new ClassReader(classfileBuffer);
		ClassNode classNode = new ClassNode();
		
		reader.accept(classNode, 0);
		
		InvokeDynamicTransfer transfer = new InvokeDynamicTransfer(classNode);
		if(!transfer.hasInvokeDynamicTransformation())
			return null;
		
		try {
			byte[] result = transfer.transfer();
			return result;
		} catch (Exception ex) {
			throw new IllegalClassFormatException();
		}
		
	}

}
