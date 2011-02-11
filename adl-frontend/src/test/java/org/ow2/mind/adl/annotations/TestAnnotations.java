
package org.ow2.mind.adl.annotations;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLFrontendModule;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestAnnotations {
  private Loader loader;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new IDLFrontendModule(),
        new ADLFrontendModule() {
          protected void configureErrorLoader() {
            bind(Loader.class).annotatedWith(Names.named("ErrorLoader"))
                .toChainStartingWith(ErrorLoader.class)
                .endingWith(Loader.class);
          }
        });

    loader = injector.getInstance(Key.get(Loader.class,
        Names.named("ErrorLoader")));
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition definition = loader.load("pkg1.annotations.SourceFlag",
        new HashMap<Object, Object>());
    final Source[] sources = castNodeError(definition,
        ImplementationContainer.class).getSources();
    for (final Source source : sources) {
      final Flag flag = AnnotationHelper.getAnnotation(source, Flag.class);
      assertEquals(flag.value, "-DFOO");
    }
  }
}
