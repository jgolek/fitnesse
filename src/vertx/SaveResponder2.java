// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package vertx;


import static fitnesse.responders.editing.SaveRecorder.changesShouldBeMerged;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.MergeResponder;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

public class SaveResponder2 {

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    long editTimeStamp = getEditTime(request);
    long ticketId = getTicketId(request);
    String resource = request.getResource();
    WikiPage page = getPage(resource, context);

    if (changesShouldBeMerged(editTimeStamp, ticketId, page))
      return new MergeResponder(request).makeResponse(context, request);
    else {
      return saveEdits(context, request, page, ticketId);
    }
  }

  private Response saveEdits(FitNesseContext context, Request request, WikiPage page, long ticketId) {
    String savedContent = request.getInput(EditResponder.CONTENT_INPUT_NAME);
    String helpText = request.getInput(EditResponder.HELP_TEXT);
    String suites = request.getInput(EditResponder.SUITES);
    String user = request.getAuthorizationUsername();
    PageData data = page.getData();

    Response response = new SimpleResponse();
    setData(data, savedContent, helpText, suites, user);
    
    SaveRecorder.pageSaved(page, ticketId);
    
    VersionInfo commitRecord = page.commit(data);
    if (commitRecord != null) {
      response.addHeader("Current-Version", commitRecord.getName());
    }
    context.recentChanges.updateRecentChanges(page);

    if (request.hasInput("redirect"))
      response.redirect("", request.getInput("redirect"));
    else
      response.redirect(context.contextRoot, request.getResource());

    return response;
  }

  private long getTicketId(Request request) {
    if (!request.hasInput(EditResponder.TICKET_ID))
      return 0;
    String ticketIdString = request.getInput(EditResponder.TICKET_ID);
    return Long.parseLong(ticketIdString);
  }

  private long getEditTime(Request request) {
    if (!request.hasInput(EditResponder.TIME_STAMP)){
        return 0;        
    }
    String editTimeStampString = request.getInput(EditResponder.TIME_STAMP);
    return Long.parseLong(editTimeStampString);
  }

  private WikiPage getPage(String resource, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    WikiPage page = pageCrawler.getPage(path);
    if (page == null){
        page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse(resource));        
    }
    return page;
  }

  private void setData(final PageData data, final String savedContent, final String helpText, final String suites, String user) {
    data.setContent(savedContent);
    data.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
    data.setOrRemoveAttribute(PageData.PropertySUITES, suites);
    data.setOrRemoveAttribute(PageData.LAST_MODIFYING_USER, user);
  }

}
