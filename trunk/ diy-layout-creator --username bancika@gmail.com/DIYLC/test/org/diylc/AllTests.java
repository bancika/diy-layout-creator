package org.diylc;

import org.diylc.plugins.file.BomMakerTest;
import org.diylc.presenter.ClassProcessorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses( { BomMakerTest.class, ClassProcessorTest.class })
public class AllTests {

}
