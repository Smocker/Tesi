package org.processmining.tests.tirocinio;

import org.junit.Test;
import org.processmining.contexts.cli.CLI;

import junit.framework.TestCase;

public class TirocinioTest extends TestCase 
{
  /**
   * Test che verifica la validita' dei plugin fornendo una lista di essi
   * 
   * @throws Throwable
   */
  @Test
  public void testTirocinio1() throws Throwable 
  {
    String args[] = new String[] {"-l"};
    CLI.main(args);
  }
  
  /**
   * Test che verifica la validita' dei plugin e li esegue utilizzando un file di test
   * 
   * @throws Throwable
   */
  @Test
  public void testTirocinio2() throws Throwable 
  {
    String testFileRoot = System.getProperty("test.testFileRoot", "./tests/testfiles");
    String args[] = new String[] {"-f", testFileRoot+"/Tirocinio_Test.txt"};
    CLI.main(args);
  }
  
  /**
   * MAIN: esegue uno dei due test
   * 
   * @param args
   */
  public static void main(String[] args) 
  {
    junit.textui.TestRunner.run(TirocinioTest.class);
  }
}