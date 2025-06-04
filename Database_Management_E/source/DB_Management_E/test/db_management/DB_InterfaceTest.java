package db_management;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Field;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.junit.Test;

import sun.misc.Unsafe;
import java.lang.reflect.Modifier;

public class DB_InterfaceTest {

    private static Unsafe getUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    private DB_Interface createInstance() throws Exception {
        Unsafe unsafe = getUnsafe();
        DB_Interface obj = (DB_Interface) unsafe.allocateInstance(DB_Interface.class);
        setField(obj, "User", new JTextField());
        setField(obj, "Password", new JPasswordField());
        setField(obj, "IPadress", new JTextField());
        setField(obj, "StatusLed", new JLabel());
        setField(obj, "Status", new JLabel());
        return obj;
    }

    private void setField(DB_Interface obj, String name, Object value) throws Exception {
        Field f = DB_Interface.class.getDeclaredField(name);
        f.setAccessible(true);
        if (Modifier.isStatic(f.getModifiers())) {
            f.set(null, value);
        } else {
            f.set(obj, value);
        }
    }

    @Test
    public void testConnectWithInvalidCredentials() throws Exception {
        System.setProperty("java.awt.headless", "true");
        DB_Interface db = createInstance();
        setFieldValue(db, "User", "invalid");
        setFieldValue(db, "Password", "invalid");
        setFieldValue(db, "IPadress", "127.0.0.1");
        assertThrows(Exception.class, () -> db.connect());
        assertNull(DB_Interface.dbcoConnection);
    }

    private void setFieldValue(DB_Interface obj, String field, String val) throws Exception {
        Field f = DB_Interface.class.getDeclaredField(field);
        f.setAccessible(true);
        Object o = f.get(obj);
        if (o instanceof JTextField) {
            ((JTextField)o).setText(val);
        } else if (o instanceof JPasswordField) {
            ((JPasswordField)o).setText(val);
        }
    }
}
