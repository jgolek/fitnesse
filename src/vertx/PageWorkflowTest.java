package vertx;

import org.junit.Test;

public class PageWorkflowTest {
    
    
    @Test
    public void testEditResponder2() throws Exception {
        
        PageWorkflow pw = new PageWorkflow();
        
        String run = pw.run("FitNesse.UserGuide", new EditResponder2());
        
        System.out.println(run);
        
    }

}
