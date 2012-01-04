package invokedynamic.agent;

import invokedynamic.InvokeDynamicUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

/**
 * An {@link Interpreter} for {@link TypeAndValue} values.
 *
 * @author Eric Bruneton
 * @author Bing Ran
 */
public class EvaluationInterepter extends Interpreter<TypeAndValue> implements
        Opcodes
{

    private final InvokeDynamicTransfer transfer;

	public EvaluationInterepter(InvokeDynamicTransfer transfer) {
        super(ASM4);
		this.transfer = transfer;
    }


    @Override
    public TypeAndValue newValue(final Type type) {
        if (type == null) {
            return TypeAndValue.UNINITIALIZED_VALUE;
        }
        switch (type.getSort()) {
            case Type.VOID:
                return null;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return TypeAndValue.INT_VALUE;
            case Type.FLOAT:
                return TypeAndValue.FLOAT_VALUE;
            case Type.LONG:
                return TypeAndValue.LONG_VALUE;
            case Type.DOUBLE:
                return TypeAndValue.DOUBLE_VALUE;
            case Type.ARRAY:
            case Type.OBJECT:
                return TypeAndValue.REFERENCE_VALUE;
            default:
                throw new Error("Internal error");
        }
    }

    public TypeAndValue newValue(final Type type, final Object value){
        if (type == null) {
            return TypeAndValue.UNINITIALIZED_VALUE;
        }
        return new TypeAndValue(type, value);
    }
    
    @Override
    public TypeAndValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return newValue(Type.getObjectType("null"), null);
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return newValue(Type.INT_TYPE, insn.getOpcode() - ICONST_0);
            case LCONST_0:
            case LCONST_1:
                return newValue(Type.LONG_TYPE, (long)(insn.getOpcode() - LCONST_0));
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return newValue(Type.FLOAT_TYPE, (float)(insn.getOpcode() - FCONST_0));
            case DCONST_0:
            case DCONST_1:
                return newValue(Type.DOUBLE_TYPE, (double)(insn.getOpcode() - DCONST_0));
            case BIPUSH:
            case SIPUSH: {
                IntInsnNode i = (IntInsnNode) insn;
                return newValue(Type.INT_TYPE, i.operand);
            }
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                if (cst instanceof Integer) {
                    Integer i = (Integer) cst;
					return newValue(Type.INT_TYPE, i);
                } else if (cst instanceof Float) {
					Float f = (Float) cst;
					return newValue(Type.FLOAT_TYPE, f);
                } else if (cst instanceof Long) { 
                    Long l = (Long) cst;
					return newValue(Type.LONG_TYPE, l);
                } else if (cst instanceof Double) {
                    Double d = (Double) cst;
					return newValue(Type.DOUBLE_TYPE, d);
                } else if (cst instanceof String) {
                    String s = (String) cst;
					return newValue(Type.getObjectType("java/lang/String"), s);
                } else if (cst instanceof Type) {
                    Type type = (Type) cst;
					int sort = ((Type) cst).getSort();
					if (sort == Type.OBJECT ) {
						String className = type.getClassName();
                        return newValue(Type.getObjectType("java/lang/Class"), getClass(className) );	
                    } if (sort == Type.ARRAY) {
                        return newValue(Type.getObjectType("java/lang/Class"), null);	// TODO
                    } else if (sort == Type.METHOD) {
                        return newValue(Type.getObjectType("java/lang/invoke/MethodType"), null);	// TODO
                    } else {
                        throw new IllegalArgumentException("Illegal LDC constant " + cst);
                    }
                } else if (cst instanceof Handle) {
                    return newValue(Type.getObjectType("java/lang/invoke/MethodHandle"), null);	// TODO
                } else {
                    throw new IllegalArgumentException("Illegal LDC constant " + cst);
                }
            case JSR:
                return TypeAndValue.RETURNADDRESS_VALUE;
            case GETSTATIC:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEW: {
            	String desc = ((TypeInsnNode) insn).desc;
            	return newValue(Type.getObjectType(desc));
            }
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public TypeAndValue copyOperation(final AbstractInsnNode insn, final TypeAndValue value)
            throws AnalyzerException
    {
        return value;
    }

    @Override
    public TypeAndValue unaryOperation(final AbstractInsnNode insn, final TypeAndValue value)
            throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case INEG: 	{
            	Integer i = (Integer) value.value();
            	return newValue(Type.INT_TYPE, -i);
            }
            case IINC: 	{
            	Integer i = (Integer)value.value();
            	return newValue(Type.INT_TYPE, 1+i);
            }
            case L2I:	{
            	Long l = (Long)value.value();
            	return newValue(Type.INT_TYPE, l.intValue());
            }
            case F2I:	{
            	Float f = (Float) value.value();
            	return newValue(Type.INT_TYPE, f.intValue());
            }
            case D2I:	{
            	Double d = (Double)value.value();
            	return newValue(Type.INT_TYPE, d.intValue());
            }
            case I2B:	{
            	Integer i = (Integer) value.value();
            	return newValue(Type.INT_TYPE, (int)i.byteValue());
            }
            case I2C: {
            	Integer i = (Integer) value.value();
            	char ch = (char)i.intValue();
            	return newValue(Type.INT_TYPE, (int)ch);            	
            }
            case I2S: {
            	Integer i = (Integer) value.value();
            	return newValue(Type.INT_TYPE, (int)i.shortValue());
            }
            case FNEG: {
            	Float f = (Float) value.value();
            	return newValue(Type.FLOAT_TYPE, -f.floatValue());
            }
            case I2F: {
            	Integer i = (Integer) value.value();
            	return newValue(Type.FLOAT_TYPE, i.floatValue());
            }
            case L2F: {
            	Long l = (Long)value.value();
            	return newValue(Type.FLOAT_TYPE, l.floatValue());
            }
            case D2F: {
            	Double d = (Double) value.value();
            	return newValue(Type.FLOAT_TYPE, d.floatValue());
            }
            case LNEG: {
            	Long l = (Long) value.value();
            	return newValue(Type.LONG_TYPE, -l);
            }
            case I2L: {
            	Integer i = (Integer) value.value();
            	return newValue(Type.LONG_TYPE, i.longValue());
            }
            case F2L: {
            	Float f = (Float) value.value();
            	return newValue(Type.LONG_TYPE, f.longValue());
            }
            case D2L: {
            	Double d = (Double) value.value();
            	return newValue(Type.LONG_TYPE, d.longValue());
            }
            case DNEG: {
            	Double d = (Double) value.value();
            	return newValue(Type.DOUBLE_TYPE, -d);
            }
            case I2D: {
            	Integer i = (Integer)value.value();
            	return newValue(Type.DOUBLE_TYPE, i.doubleValue());
            }
            case L2D: {
            	Long l = (Long) value.value();
            	return newValue(Type.DOUBLE_TYPE, l.doubleValue());
            }
            case F2D: {
            	Float f = (Float) value.value();
            	return newValue(Type.DOUBLE_TYPE, f.doubleValue());
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            	return null;
            case PUTSTATIC: {
            	FieldInsnNode i = (FieldInsnNode) insn;
            	transfer.checkBootStrapMethodArgs(i, value);
                return null;
            }
            case GETFIELD:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEWARRAY: {
            	int length = (Integer)value.value();
                switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN:
                        return newValue(Type.getType("[Z"), new boolean[length]);
                    case T_CHAR:
                        return newValue(Type.getType("[C"), new char[length]);
                    case T_BYTE:
                        return newValue(Type.getType("[B"), new byte[length]);
                    case T_SHORT:
                        return newValue(Type.getType("[S"), new short[length]);
                    case T_INT:
                        return newValue(Type.getType("[I"), new int[length]);
                    case T_FLOAT:
                        return newValue(Type.getType("[F"), new float[length]);
                    case T_DOUBLE:
                        return newValue(Type.getType("[D"), new double[length]);
                    case T_LONG:
                        return newValue(Type.getType("[J"), new long[length]);
                    default:
                        throw new AnalyzerException(insn, "Invalid array type");
                }
            }
            case ANEWARRAY: {
                String desc = ((TypeInsnNode) insn).desc;
                Class<?> type = getClass(desc);
                int length = (Integer)value.value();
                return newValue(Type.getType("[" + Type.getObjectType(desc)), Array.newInstance(type, length));
            }
            case ARRAYLENGTH:
                return TypeAndValue.INT_VALUE;
            case ATHROW:
                return null;
            case CHECKCAST: {
                String desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getObjectType(desc));
            }
            case INSTANCEOF:
                return TypeAndValue.INT_VALUE;
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public TypeAndValue binaryOperation(
        final AbstractInsnNode insn,
        final TypeAndValue value1,
        final TypeAndValue value2) throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return TypeAndValue.INT_VALUE;
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                return TypeAndValue.FLOAT_VALUE;
            case LALOAD:
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return TypeAndValue.LONG_VALUE;
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                return TypeAndValue.DOUBLE_VALUE;
            case AALOAD:
                return TypeAndValue.REFERENCE_VALUE;
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return TypeAndValue.INT_VALUE;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public TypeAndValue ternaryOperation(
        final AbstractInsnNode insn,
        final TypeAndValue array,
        final TypeAndValue index,
        final TypeAndValue value) throws AnalyzerException
    {
    	switch(insn.getOpcode()){
	        case Opcodes.IASTORE: break;
	        case Opcodes.LASTORE: break;
	        case Opcodes.FASTORE: break;
	        case Opcodes.DASTORE: break;
	        case Opcodes.AASTORE: {
	        	if(array.notnull() && index.notnull() && value.hasValue()) {
	        		Object[] dest = (Object[]) array.value();
	        		int	i = (Integer) index.value();
	        		Object v = value.value();
	        		dest[i] = v;
	        	}
	        }
	        case Opcodes.BASTORE: break;
	        case Opcodes.CASTORE: break;
	        case Opcodes.SASTORE: break;
    	}
        return null;
    }

    @Override
    public TypeAndValue naryOperation(final AbstractInsnNode insn, final List<? extends TypeAndValue> values)
            throws AnalyzerException
    {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC){
            return newValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc));
        } else if(opcode == INVOKESTATIC) {
        	MethodInsnNode method = (MethodInsnNode) insn;
        	// invokedynamic/InvokeDynamicUtils, BootStrap, ([Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;
        	if(method.owner.equals(InvokeDynamicUtils.class.getName().replace('.', '/')) && method.name.equals("BootStrap") 
        			&& method.desc.equals("([Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;") ){
        		TypeAndValue arg = values.get(0);
        		if(arg != null && arg.hasValue()){
        			Object[] paras = (Object[]) arg.value();
        			return newValue(Type.getReturnType(method.desc), new DumpMethodHandle(paras));
        		}
        	}
        	return null;
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }

    @Override
    public void returnOperation(
        final AbstractInsnNode insn,
        final TypeAndValue value,
        final TypeAndValue expected) throws AnalyzerException
    {
    }

    @Override
    public TypeAndValue merge(final TypeAndValue v, final TypeAndValue w) {
        if (!v.equals(w)) {
            return TypeAndValue.UNINITIALIZED_VALUE;
        }
        return v;
    }
    
    private Class<?> getClass(String desc){
    	try {
    		return Class.forName(desc.replace('/', '.'));
    	}
    	catch(Exception ex){
    		throw new RuntimeException(ex);
    	}
    }
    
    static class DumpMethodHandle {

		Object[] args;

		DumpMethodHandle(Object[] args) {
			this.args = args;
		}
    
		
    }
}
