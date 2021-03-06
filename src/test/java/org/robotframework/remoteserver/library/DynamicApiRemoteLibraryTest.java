package org.robotframework.remoteserver.library;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;

import org.robotframework.remoteserver.testlibraries.DynamicOneRunKeywordKwargs;
import org.robotframework.remoteserver.testlibraries.DynamicOneRunKeywordKwargsList;
import org.robotframework.remoteserver.testlibraries.DynamicOneRunKeywordNoKwargs;
import org.robotframework.remoteserver.testlibraries.DynamicUsingLists;
import org.testng.annotations.Test;

public class DynamicApiRemoteLibraryTest {

    private static Object[] noPosArgs = new Object[] {};
    private static Map<String, Object> noKwargs = new HashMap<String, Object>();

    @Test
    public void getDynamicAPILibraryImplementation() throws Exception {
        DynamicOneRunKeywordNoKwargs lib = new DynamicOneRunKeywordNoKwargs();
        Method gkn = DynamicOneRunKeywordNoKwargs.class.getMethod("getKeywordNames");
        Method rk = DynamicOneRunKeywordNoKwargs.class.getMethod("runKeyword", new Class<?>[] { String.class,
                Object[].class });
        DynamicApiRemoteLibrary wrapper = new DynamicApiRemoteLibrary(lib, gkn, rk, null, null);
        assertEquals(wrapper.getImplementation(), lib);
    }

    @Test
    public void libraryUsingLists() throws Exception {
        DynamicUsingLists lib = new DynamicUsingLists();
        Method gkn = DynamicUsingLists.class.getMethod("getKeywordNames");
        Method rk = DynamicUsingLists.class.getMethod("runKeyword", new Class<?>[] { String.class, List.class });
        Method gka = DynamicUsingLists.class.getMethod("getKeywordArguments", new Class<?>[] { String.class });
        DynamicApiRemoteLibrary wrapper = new DynamicApiRemoteLibrary(lib, gkn, rk, gka, null);
        Assert.assertEquals(wrapper.getKeywordNames(), new String[] { "go" });
        Assert.assertEquals(wrapper.runKeyword("go", new Object[] { "there" }, noKwargs), "there");
        Assert.assertEquals(wrapper.getKeywordArguments("go"), new String[] { "where" });
    }

    @Test
    public void kwargsNotSupported() throws Exception {
        DynamicApiRemoteLibrary wrapper = getWrapper(DynamicOneRunKeywordNoKwargs.class);
        String msg = null;
        try {
            wrapper.runKeyword("getArgs", noPosArgs, getKwargs("number", 42));
        } catch (RuntimeException e) {
            msg = e.getMessage();
        }
        Assert.assertEquals(msg, "This library does not support keyword arguments.");
    }

    @Test
    public void oneRunKeywordNoKwargs() throws Exception {
        DynamicApiRemoteLibrary wrapper = getWrapper(DynamicOneRunKeywordNoKwargs.class);
        String result = (String) wrapper.runKeyword("getArgs", new Object[] { "eggs" }, new HashMap<String, Object>());
        Assert.assertEquals(result, "['eggs']");
    }

    @Test
    public void oneRunKeywordWithKwargs() throws Exception {
        DynamicApiRemoteLibrary wrapper = getWrapper(DynamicOneRunKeywordKwargs.class);
        String result = (String) wrapper.runKeyword("getArgs", new Object[] { "spam" }, noKwargs);
        Assert.assertEquals(result, "['spam']{}");
        result = (String) wrapper.runKeyword("getArgs", noPosArgs, getKwargs("number", 42));
        Assert.assertEquals(result, "[]{'number':'42'}");
    }

    @Test
    public void oneRunKeywordWithKwargsWithList() throws Exception {
        DynamicApiRemoteLibrary wrapper = getWrapper(DynamicOneRunKeywordKwargsList.class);
        String result = (String) wrapper.runKeyword("getArgs", new Object[] { "spam" }, noKwargs);
        Assert.assertEquals(result, "['spam']{}");
        result = (String) wrapper.runKeyword("getArgs", noPosArgs, getKwargs("number", 42));
        Assert.assertEquals(result, "[]{'number':'42'}");
    }

    private DynamicApiRemoteLibrary getWrapper(Class<?> clazz) {
        DefaultRemoteLibraryFactory fact = new DefaultRemoteLibraryFactory();
        try {
            return (DynamicApiRemoteLibrary) fact.createRemoteLibrary(clazz.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getKwargs(Object... items) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < items.length; i += 2) {
            map.put((String) items[i], items[i + 1]);
        }
        return map;
    }

}
