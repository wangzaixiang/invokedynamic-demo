package invokedynamic;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

public class TypeAndValue implements Value {

    public static final TypeAndValue UNINITIALIZED_VALUE = new TypeAndValue(null);

    public static final TypeAndValue INT_VALUE = new TypeAndValue(Type.INT_TYPE);

    public static final TypeAndValue FLOAT_VALUE = new TypeAndValue(Type.FLOAT_TYPE);

    public static final TypeAndValue LONG_VALUE = new TypeAndValue(Type.LONG_TYPE);

    public static final TypeAndValue DOUBLE_VALUE = new TypeAndValue(Type.DOUBLE_TYPE);

    public static final TypeAndValue REFERENCE_VALUE = new TypeAndValue(Type.getObjectType("java/lang/Object"));

    public static final TypeAndValue RETURNADDRESS_VALUE = new TypeAndValue(Type.VOID_TYPE);

	public static final Object UnknownValue = new Object();
	
	private	 final Object	value;
	
    private final Type type;
    
	public TypeAndValue(Type type) {
		this.type = type;
		value = UnknownValue;
	}


    public TypeAndValue(Type type, Object value) {
		this.type = type;
		this.value = value;
	}

    public Object value(){
    	return value;
    }
    
    public boolean hasValue(){
    	return value != UnknownValue;
    }
    
    public boolean notnull(){
    	return value != null && value != UnknownValue;
    }

	public Type getType() {
        return type;
    }

    public int getSize() {
        return type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ? 2 : 1;
    }

    public boolean isReference() {
        return type != null
                && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
    }
    
    
    @Override
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        } else if (value instanceof BasicValue) {
            if (type == null) {
                return ((TypeAndValue) value).type == null;
            } else {
                return type.equals(((TypeAndValue) value).type);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return type == null ? 0 : type.hashCode();
    }

    @Override
    public String toString() {
        if (this == UNINITIALIZED_VALUE) {
            return ".";
        } else if (this == RETURNADDRESS_VALUE) {
            return "A";
        } else if (this == REFERENCE_VALUE) {
            return "R";
        } else {
            return type.getDescriptor();
        }
    }

}
