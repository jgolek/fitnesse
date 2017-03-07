package vertx;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fitnesse.wiki.WikiPage;

public class PageWorkflowTest {
    
    
    @Test
    public void testEditResponder2() throws Exception {
        
        PageWorkflow pw = new PageWorkflow();
        
        String run = pw.run("FitNesse.UserGuide", new EditResponder2());
        
        System.out.println(run);
        
    }
    
    
    @Test
    public void testAddChildPage() throws Exception {
        
        PageWorkflow pw = new PageWorkflow();
        
        Map<String, String> params = new HashMap<>();
        params.put("pageName", "TestTest");
        WikiPage page = pw.createPage("FrontPage", new AddChildPageResponder2(), params);
        
        assertEquals("FrontPage.TestTest", page.getName());
        
    }

}
