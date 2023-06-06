/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

public class NativeArrayTest {
  private NativeArray array;

  @Before
  public void init() {
    array = new NativeArray(1);
  }

  @Test
  public void getIdsShouldIncludeBothIndexAndNormalProperties() {
    array.put(0, array, "index");
    array.put("a", array, "normal");

    assertThat(array.getIds(), is(new Object[]{0, "a"}));
  }

  @Test
  public void deleteShouldRemoveIndexProperties() {
    array.put(0, array, "a");
    array.delete(0);
    assertThat(array.has(0, array), is(false));
  }

  @Test
  public void deleteShouldRemoveNormalProperties() {
    array.put("p", array, "a");
    array.delete("p");
    assertThat(array.has("p", array), is(false));
  }

  @Test
  public void putShouldAddIndexProperties() {
    array.put(0, array, "a");
    assertThat(array.has(0, array), is(true));
  }

  @Test
  public void putShouldAddNormalProperties() {
    array.put("p", array, "a");
    assertThat(array.has("p", array), is(true));
  }

  @Test
  public void getShouldReturnIndexProperties() {
    array.put(0, array, "a");
    array.put("p", array, "b");
    assertThat((String) array.get(0, array), is("a"));
  }

  @Test
  public void getShouldReturnNormalProperties() {
    array.put("p", array, "a");
    assertThat((String) array.get("p", array), is("a"));
  }

  @Test
  public void hasShouldBeFalseForANewArray() {
    assertThat(new NativeArray(0).has(0, array), is(false));
  }

  @Test
  public void getIndexIdsShouldBeEmptyForEmptyArray() {
    assertThat(new NativeArray(0).getIndexIds(), is(new ArrayList<Integer>()));
  }

  @Test
  public void getIndexIdsShouldBeAZeroForSimpleSingletonArray() {
    array.put(0, array, "a");
    assertThat(array.getIndexIds(), is(Arrays.asList(0)));
  }

  @Test
  public void getIndexIdsShouldWorkWhenIndicesSetAsString() {
    array.put("0", array, "a");
    assertThat(array.getIndexIds(), is(Arrays.asList(0)));
  }

  @Test
  public void getIndexIdsShouldNotIncludeNegativeIds() {
    array.put(-1, array, "a");
    assertThat(array.getIndexIds(), is(new ArrayList<Integer>()));
  }

  @Test
  public void getIndexIdsShouldIncludeIdsLessThan2ToThe32() {
    int maxIndex = (int) (1L << 31) - 1;
    array.put(maxIndex, array, "a");
    assertThat(array.getIndexIds(), is(Arrays.asList(maxIndex)));
  }

  @Test
  public void getIndexIdsShouldNotIncludeIdsGreaterThanOrEqualTo2ToThe32() {
    array.put((1L<<31)+"", array, "a");
    assertThat(array.getIndexIds(), is(new ArrayList<Integer>()));
  }

  @Test
  public void getIndexIdsShouldNotReturnNonNumericIds() {
    array.put("x", array, "a");
    assertThat(array.getIndexIds(), is(new ArrayList<Integer>()));
  }

  @Test
  public void testToString() {
      String source =
          "var f = function() {\n"
          + "  var obj = [0,1];\n"
          + "  var a = obj.map(function() {return obj;});\n"
          + "  return a.toString();\n"
          + "};\n"
          + "f();";

      Context cx = Context.enter();
      try {
          cx.setLanguageVersion(Context.VERSION_ES6);

          Scriptable scope = cx.initStandardObjects();
          String result = cx.evaluateString(scope, source, "source", 1, null).toString();
          Assert.assertEquals("0,1,0,1", result);
      } finally {
          Context.exit();
      }
  }

    @Test
    public void testFlat() {
        String source =
                "var array = [0,1,[2,3],4,[5],[6,7,8]];\n" +
                "JSON.stringify(array.flat(1))";

        String expected = "[0,1,2,3,4,5,6,7,8]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatNoArgument() {
        String source =
                "var array = [0,1,[2,3],4,[5],[6,7,8]];\n" +
                        "JSON.stringify(array.flat())";

        String expected = "[0,1,2,3,4,5,6,7,8]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatDepth1() {
        String source =
                "var array = [0,1,[2,3],4,[5],[6,7,[8,9]]];\n" +
                        "JSON.stringify(array.flat(1))";

        String expected = "[0,1,2,3,4,5,6,7,[8,9]]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatEmpty() {
        String source =
                "var a = {};\n" +
                        "JSON.stringify([[],[1, a]].flat());";

        String expected = "[1,{}]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatNull() {
        String source =
                "var a = [void 0]; var array = [1,[null,a, undefined]];\n" +
                        "JSON.stringify(array.flat())";

        String expected = "[1,null,null]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatDepth2() {
        String source =
                "var array = [0,1,[2,3],4,[5],[6,7,[8,9]]];\n" +
                        "JSON.stringify(array.flat(2))";

        String expected = "[0,1,2,3,4,5,6,7,8,9]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatDepth1Debug() {
        String source =
                "var array = [[6,7,[8,9]]];\n" +
                        "JSON.stringify(array.flat(1))";

        String expected = "[6,7,[8,9]]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatMap() {
        String source =
                "var array = [1, 2, 3, 4];\n" +
                "JSON.stringify(array.flatMap(x => [x, x * 2]));";

        String expected = "[1,2,2,4,3,6,4,8]";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatLength() {
        String source = "Array.prototype.flat.length == 0";

        String expected = "true";

        executeStringTest(source, expected);
    }

    @Test
    public void testFlatArrayLike() {
        String source = "Array.prototype.flat.length == 0";

        String expected = "true";

        executeStringTest(source, expected);
    }

    private void executeStringTest(String source, String expected) {
        Context cx = Context.enter();
        try {
            cx.setLanguageVersion(Context.VERSION_ES6);

            cx.setOptimizationLevel(-1);

            Scriptable scope = cx.initStandardObjects();
            String result = cx.evaluateString(scope, source, "source", 1, null).toString();
            Assert.assertEquals(expected, result);
        } finally {
            Context.exit();
        }
    }
}
