// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package vertx.responders;

import fitnesse.html.HtmlUtil;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageFactory;
import fitnesse.html.template.PageTitle;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.responders.editing.TemplateUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class EditResponder2 {
  public static final String CONTENT_INPUT_NAME = "pageContent";
  public static final String TIME_STAMP = "editTime";
  public static final String TICKET_ID = "ticketId";
  public static final String HELP_TEXT = "helpText";
  public static final String SUITES = "suites";
  public static final String PAGE_TYPE = "pageType";
  public static final String PAGE_NAME = "pageName";
  public static final String TEMPLATE_MAP = "templateMap";


  public String makeHtml(WikiPage page, PageFactory pageFactory) {
      
    PageData pageData = page.getData();
    String title = "Edit:";
      
    HtmlPage html = pageFactory.newPage();
    html.setTitle(title + page.getName());

    WikiPagePath pagePath = page.getPageCrawler().getFullPath();
    String qualifiedPageName = pagePath.toString();
    html.setPageTitle(new PageTitle(title + " Page:", pagePath, pageData.getAttribute(PageData.PropertySUITES)));
    html.setMainTemplate("editPage");

    html.put("resource", qualifiedPageName);
    html.put(TIME_STAMP, String.valueOf(SaveRecorder.timeStamp()));
    html.put(TICKET_ID, String.valueOf(SaveRecorder.newTicket()));

    html.put(HELP_TEXT, pageData.getAttribute(PageData.PropertyHELP));
    html.put(TEMPLATE_MAP, TemplateUtil.getTemplateMap(page));
    html.put("suites", pageData.getAttribute(PageData.PropertySUITES));
    html.put(CONTENT_INPUT_NAME, HtmlUtil.escapeHTML(pageData.getContent()));
    
    return html.html();
  }


}
