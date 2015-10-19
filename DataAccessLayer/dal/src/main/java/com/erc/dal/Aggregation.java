package com.erc.dal;

/**
 * Created by einar on 10/15/2015.
 */
public class Aggregation {

    public static final String AVG = " AVG(" + Ctt.VALUE + ") ";
    public static final String SUM = " SUM(" + Ctt.VALUE + ") ";
    public static final String MAX = " MAX(" + Ctt.VALUE + ") ";
    public static final String MIN = " MIN(" + Ctt.VALUE + ") ";
    public static final String COUNT = " COUNT(" + Ctt.VALUE + ") ";

    private String field;
    private String operator;

    private Aggregation(String field, String operator) {
        this.field = field;
        this.operator = operator;
    }

    public static Aggregation avg(String field) {
        return new Aggregation(field, Aggregation.AVG);
    }

    public static Aggregation sum(String field) {
        return new Aggregation(field, Aggregation.SUM);
    }

    public static Aggregation max(String field) {
        return new Aggregation(field, Aggregation.MAX);
    }

    public static Aggregation min(String field) {
        return new Aggregation(field, Aggregation.MIN);
    }

    public static Aggregation count() {
        return new Aggregation("*", Aggregation.COUNT);
    }

    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator;
    }

    public String toString(Entity entity) {
        String res = "";
        if (entity != null) {
            try {
                if (!getField().equals("*")) {
                    String fieldName = ReflectionHelper.getFieldNameFromDBName(entity, getField());
                    java.lang.reflect.Field field = entity.getClass().getField(fieldName);
                }
                res = getOperator().replace(Ctt.VALUE, getField());
            } catch (NoSuchFieldException e) {
                Log.e("null field: " + getField(), e);
            }
        } else {
            Log.w("null entity on getAggregation");
        }
        return res;
    }
}
