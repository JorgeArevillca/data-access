package com.erc.dal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yhoseph on 20-10-15.
 */
public class DB {
    DBConfig dbConfig;

    public DB(Context context) {
        this.dbConfig = new DBConfig(context, null, 1, null);
        DBManager.open(this.dbConfig);
    }

    public DB(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
        DBManager.open(this.dbConfig);
    }

    public Entity save(Entity entity) {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(entity);
        if (pair != null) {
            if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
                QueryBuilder.setID(entity, this);
                sql = QueryBuilder.getQueryInsert(entity);
            } else {
                sql = QueryBuilder.getQueryUpdate(entity);
            }
            execSQL(sql);
            return entity;
        } else {
            return null;
        }
    }

    public <T> T getById(Class classType, long id) {
        T entity = null;
        String sql = QueryBuilder.getQuery(classType, id);
        if (!Util.isNullOrEmpty(sql)) {
            Cursor cursor = rawQuery(sql);
            if (cursor.moveToNext()) {
                try {
                    entity = (T) ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                    ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                    fillFields(fields, cursor, entity);
                } catch (Exception e) {
                    Log.e("Fail to fill getById", e);
                }
            }
            return entity;
        } else {
            return null;
        }
    }

    public <T> ArrayList<T> getAll(Class classType, Options... options) {
        ArrayList<T> entities = new ArrayList<>();
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(classType);
        selectAll = options_.getSql(classType, selectAll) + Ctt.SEMICOLON;
        Cursor cursor = rawQuery(selectAll);
        while (cursor.moveToNext()) {
            try {
                Object entity = ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
                entities.add((T) entity);
            } catch (Exception e) {
                Log.e("Fail to fill getAll", e);
            }
        }
        return entities;
    }

    public long calculate(Class classType, Aggregation aggregationOperator, Options... options) {
        long res = 0;
        try {
            if (aggregationOperator != null) {
                Options options_ = options.length == 0 ? new Options() : options[0];
                String selectAll = QueryBuilder.getAllQuery(classType);
                selectAll = options_.getSql(classType, selectAll, aggregationOperator) + Ctt.SEMICOLON;
                Cursor cursor = rawQuery(selectAll);
                if (cursor.moveToNext()) {
                    res = cursor.getLong(0);
                }
            } else {
                Log.w("null aggregation Operator on Entity.Calculate");
            }
        } catch (Exception e) {
            Log.e("fail to calculate:" + aggregationOperator.getOperator(), e);
        }
        return res;
    }

    public boolean remove(Class classType, int id) {
        String sql = QueryBuilder.getQueryRemove(classType, id);
        if (Util.isNullOrEmpty(sql)) {
            return execSQL(sql);
        }
        return false;
    }

    private Cursor rawQuery(String sql) {
        SQLiteDatabase db = DBManager.openReadOnly(dbConfig);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
        } catch (Exception e) {
            Log.e("Failed to execute raw SQL", e);
        }
        return cursor;
    }

    public boolean execSQL(String sql) {
        SQLiteDatabase db = DBManager.open(dbConfig);
        boolean res = false;
        try {
            db.execSQL(sql);
            //Log.i(sql);
            res = true;
        } catch (Exception e) {
            Log.e("Failed to execute SQL", e);
        }
        return res;
    }

    private static void fillFields(ArrayList<java.lang.reflect.Field> fields, Cursor cursor, Object entity) throws IllegalAccessException {
        for (java.lang.reflect.Field field : fields) {
            Object value = null;
            Type type = field.getType();
            do {
                if (type.equals(String.class)) {
                    value = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(char.class) || type.equals(Character.class)) {
                    String charText = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    value = Util.isNullOrEmpty(charText) ? '\0' : charText.toCharArray()[0];
                    break;
                }
                if (type.equals(Date.class)) {
                    String dateIso = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    value = HelperDate.getDateFromFormat(dateIso, HelperDate.ISO_FORMAT);
                    break;
                }
                if (type.equals(short.class) || type.equals(Short.class)) {
                    value = cursor.getShort(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(int.class) || type.equals(Integer.class)) {
                    value = cursor.getInt(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(long.class) || type.equals(Long.class)) {
                    value = cursor.getLong(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    value = cursor.getInt(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field))) == 1;
                    break;
                }
                if (type.equals(double.class) || type.equals(Double.class)) {
                    value = cursor.getDouble(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(float.class) || type.equals(Float.class)) {
                    value = cursor.getFloat(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                break;
            } while (true);

            field.set(entity, value);
        }
    }
}
