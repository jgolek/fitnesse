package vertx;

import static org.junit.Assert.*;

import org.junit.Test;

public class PageWorkflowTest {
    
    
    @Test
    public void testName() throws Exception {
        
        PageWorkflow pw = new PageWorkflow();
        
        String run = pw.run();
        
        System.out.println(run);
        
    }

}
