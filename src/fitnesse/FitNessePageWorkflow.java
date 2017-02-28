package fitnesse;

import java.io.IOException;

import fitnesse.http.Request;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiPageResponder;

public class FitNessePageWorkflow {

    public void run() throws InstantiationException, IOException{
     
        String rootPath = "./";
        ResponderFactory responderFactory = new ResponderFactory(rootPath); 
        
        Request request = null;
        Responder responder = responderFactory.makeResponder(request);
               
        
        WikiPageResponder  wpr;
        
        //todo rewrite the responders.
        
        //load page
        
        //generate html
        
        //generate response.
        
        //todo write vertex service. why vertex? its's simple, java and its scale. 
        
        
    }
    
}
