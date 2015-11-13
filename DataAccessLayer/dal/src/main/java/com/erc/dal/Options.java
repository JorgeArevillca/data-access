package com.erc.dal;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by einar on 9/6/2015.
 */
public class Options {


    private String orderBy;
    private String limit;
    private boolean ascending;
    private boolean distinct;
    private ArrayList<Expresion> expresions;
    private Class entityClass;
    String tableName;


    public Options() {
        expresions = new ArrayList<Expresion>();
    }

    public void and(String fieldName, Object value, ExpresionOperator... expresionOperator) {
        String value_ = Util.getValueFromObject(value);
        if (!Util.isNullOrEmpty(fieldName) && !Util.isNullOrEmpty(value_)) {
            ExpresionOperator expresionOperator_ = expresionOperator.length == 0 ? ExpresionOperator.EQUALS : expresionOperator[0];
            expresions.add(new Expresion(fieldName, expresionOperator_, value_, LogicalOperator.AND));
        } else {
            Log.w("Null or empty value on Options.and: " + fieldName + ", " + value);
        }
    }

    public void or(String fieldName, Object value, ExpresionOperator... expresionOperator) {
        String value_ = Util.getValueFromObject(value);
        if (!Util.isNullOrEmpty(fieldName) && !Util.isNullOrEmpty(value_)) {
            ExpresionOperator expresionOperator_ = expresionOperator.length == 0 ? ExpresionOperator.EQUALS : expresionOperator[0];
            expresions.add(new Expresion(fieldName, expresionOperator_, value_, LogicalOperator.OR));
        } else {
            Log.w("Null or empty value on Options.or: " + fieldName + ", " + value);
        }
    }

    public void orderBy(String fieldName, boolean ascending) {
        this.orderBy = fieldName;
        this.ascending = ascending;
    }

    public void distinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void in(String fieldName, ArrayList values, LogicalOperator... logicalOperator) {
        LogicalOperator logicalOperator_ = logicalOperator.length == 0 ? LogicalOperator.AND : logicalOperator[0];
        ArrayList<String> items = new ArrayList<>();
        if (values.size() > 0) {
            boolean hasQuotes = HelperDataType.hasCuotes(values.get(0));
            for (Object item : values) {
                items.add((hasQuotes ? Constant.VALUE_QUOTES : Constant.VALUE).replaceFirst(Constant.VALUE, Util.getValueFromObject(item)));
            }
            Expresion expresion = new Expresion(fieldName, ExpresionOperator.IN, "(" + TextUtils.join(",", items) + ")", logicalOperator_);
            expresion.setIgnoreQuotes(true);
            expresions.add(expresion);
        }
    }

    public void limit(int limit) {
        this.limit = limit + "";
    }

    public String getSql(Class entityClass, String selectAllSQL, Aggregation... aggregation) {
        this.entityClass = entityClass;
        tableName = QueryBuilder.geTableName(entityClass);

        StringBuffer sb = new StringBuffer();

        if (getDistinct()) {
            selectAllSQL = selectAllSQL.replace(Constant.SELECT, Constant.SELECT + Constant.DISTINCT);
        }

        if (aggregation.length > 0) {
            Aggregation aggregation_ = aggregation[0];
            clearForAggregation();
            String aggregationText = aggregation_.toString(entityClass);
            if (!Util.isNullOrEmpty(aggregationText)) {
                selectAllSQL = selectAllSQL.replace("*", aggregationText);
            }
        }
        sb.append(selectAllSQL);
        String expresions = getExpressions();
        if (expresions.length() > 0) {
            sb.append(Constant.WHERE);
            sb.append(expresions);
        }
        sb.append(getOrderBy());
        sb.append(getLimit());

        return sb.toString();
    }

    private String getExpressions() {
        StringBuffer sb = new StringBuffer();
        if (!Util.isNullOrEmpty(tableName)) {
            int i = 0;
            for (Expresion e : expresions) {
                if (i > 0) {
                    sb.append(e.getLogicalOperator());
                }
                try {
                    Class type = entityClass.getField(e.getLeft()).getType();
                    sb.append(e.getExpresionString(tableName, HelperDataType.hasCuotes(type)));
                    i++;
                } catch (NoSuchFieldException e1) {
                    Log.e("Invalid Expresion on  getExpressions", e1);
                }
            }
        } else {
            Log.w("Null TableName on getExpressions");
        }
        return sb.toString();
    }

    private String getOrderBy() {
        String res = "";
        if (!Util.isNullOrEmpty(tableName)) {
            if (!Util.isNullOrEmpty(orderBy)) {
                res = Constant.ORDER_BY + tableName + "." + orderBy + (ascending ? Constant.ASC : Constant.DESC);
            }
        } else {
            Log.w("Null TableName on getOrderBy");
        }
        return res;
    }

    private boolean getDistinct() {
        return distinct;
    }

    private String getLimit() {
        String res = "";
        if (!Util.isNullOrEmpty(limit))
            res = Constant.LIMIT + limit;
        return res;
    }

    private void clearForAggregation() {
        if (distinct) {
            Log.w("'Distinct' will be ignored... in order to use aggregation operator");
        }
        distinct = false;
    }

}
