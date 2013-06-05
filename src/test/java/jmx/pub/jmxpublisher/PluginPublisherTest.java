package jmx.pub.jmxpublisher;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

public class PluginPublisherTest extends HudsonTestCase {
	  private String jmxPath = "jmx_data.txt";
	  private boolean threshold = false;
	
	
  public void testConfig() throws Exception {
/*    PluginPublisher publisher = new PluginPublisher(jmxPath, threshold);
    FreeStyleProject p = createFreeStyleProject();
    p.getPublishersList().add(publisher);
    submit(createWebClient().getPage(p, "configure").getFormByName("config"));
    
    PluginPublisher afterPublisher = p.getPublishersList().get(
      PluginPublisher.class);

    assertEqualBeans(publisher,afterPublisher,"jmxPath");
 */ }

  public void testBuild() throws Exception {
  /*  FreeStyleProject p = createFreeStyleProject();
    p.getBuildersList().add(new TestBuilder() {
      @Override
      public boolean perform(AbstractBuild<?, ?> build,
          Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        build.getWorkspace().child("jmx_data.txt").copyFrom(
            getClass().getResource("/jmx_data.txt"));
          return true;
      }
    });
    p.getPublishersList().add(
    		new PluginPublisher(jmxPath, threshold));
    FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0).get());
    JMXPublisherBuildAction a = b.getAction(JMXPublisherBuildAction.class);
    assertNotNull(a);

    // poke a few random pages to verify rendering
    WebClient wc = createWebClient();
    wc.getPage(b, "JMXPublisher");
*/  }
}
