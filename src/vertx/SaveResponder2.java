// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package vertx;


import static fitnesse.responders.editing.SaveRecorder.changesShouldBeMerged;

import java.util.Map;

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
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

public class SaveResponder2 {

    public void updatePage(WikiPage page, Map<String, String> params) {

        //long editTimeStamp = getEditTime(params);
        long ticketId = getTicketId(params);
        saveEdits(params, page, ticketId);
    }

    private void saveEdits(Map<String, String> params, WikiPage page, long ticketId) {
        String savedContent = params.get(EditResponder.CONTENT_INPUT_NAME);
        String helpText = params.get(EditResponder.HELP_TEXT);
        String suites = params.get(EditResponder.SUITES);
        PageData data = page.getData();

        Response response = new SimpleResponse();
        setData(data, savedContent, helpText, suites);

        SaveRecorder.pageSaved(page, ticketId);

        VersionInfo commitRecord = page.commit(data);
        if (commitRecord != null) {
            response.addHeader("Current-Version", commitRecord.getName());
        }
        
        RecentChangesWikiPage recentChanges = new RecentChangesWikiPage();
        recentChanges.updateRecentChanges(page);
    }

    private long getTicketId(Map<String, String> params) {
        if (!params.containsKey(EditResponder.TICKET_ID))
            return 0;
        String ticketIdString = params.get(EditResponder.TICKET_ID);
        return Long.parseLong(ticketIdString);
    }

    private long getEditTime(Map<String, String> params) {
        if (!params.containsKey(EditResponder.TIME_STAMP)) {
            return 0;
        }
        String editTimeStampString = params.get(EditResponder.TIME_STAMP);
        return Long.parseLong(editTimeStampString);
    }

    private WikiPage getPage(String resource, FitNesseContext context) {
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
        WikiPage page = pageCrawler.getPage(path);
        if (page == null) {
            page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse(resource));
        }
        return page;
    }

    private void setData(final PageData data, final String savedContent, final String helpText, final String suites) {
        data.setContent(savedContent);
        data.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
        data.setOrRemoveAttribute(PageData.PropertySUITES, suites);
    }

}
