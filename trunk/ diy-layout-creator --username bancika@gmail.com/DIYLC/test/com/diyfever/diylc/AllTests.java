package com.diyfever.diylc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.diyfever.diylc.plugins.file.BomMakerTest;
import com.diyfever.diylc.presenter.ClassProcessorTest;

@RunWith(Suite.class)
@SuiteClasses( { BomMakerTest.class, ClassProcessorTest.class })
public class AllTests {

}
