package vertx;

import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.NewPageResponder;
import fitnesse.wiki.*;

public class AddChildPageResponder2 {

    public WikiPage createPage(PageCrawler pageCrawler, String qualifiedPageName, Map<String, String> params)
                    throws Exception {

        String pageType = null;
        WikiPage pageTemplate = null;

        String childName = params.get(EditResponder.PAGE_NAME);
        childName = childName == null ? "null" : childName;
        WikiPagePath childPath = PathParser.parse(childName);
        WikiPagePath currentPagePath = PathParser.parse(qualifiedPageName);
        WikiPage currentPage = pageCrawler.getPage(currentPagePath);

        if (params.containsKey(NewPageResponder.PAGE_TEMPLATE)) {
            pageTemplate = pageCrawler.getPage(PathParser.parse(params.get(NewPageResponder.PAGE_TEMPLATE)));
        } else {
            pageType = params.get(EditResponder.PAGE_TYPE);
        }

        String childContent = params.get(EditResponder.CONTENT_INPUT_NAME);
        String helpText = params.get(EditResponder.HELP_TEXT);
        String suites = params.get(EditResponder.SUITES);
        if (childContent == null) {
            childContent = "!contents\n";
        }
        if (pageTemplate == null && pageType == null) {
            pageType = "Default";
        }

//        if (currentPage == null)
//            return notFoundResponse(context, request);
//        else if (nameIsInvalid(childName))
//            return errorResponse(context, request);
//        else if (pageAlreadyExists(childName))
//            return alreadyExistsResponse(context, request);

        RecentChangesWikiPage recentChanges = new RecentChangesWikiPage();
        
        //createChildPage
        WikiPage childPage = WikiPageUtil.addPage(currentPage, childPath, childContent); //saved here a page
        PageData childPageData = childPage.getData();
        if (pageTemplate != null) {
            childPageData.setProperties(pageTemplate.getData().getProperties());
        } else if (pageType.equals("Static")) {
            childPageData.getProperties().remove("Test");
            childPageData.getProperties().remove("Suite");
        } else if ("Test".equals(pageType) || "Suite".equals(pageType)) {
            childPageData.getProperties().remove("Test");
            childPageData.getProperties().remove("Suite");
            childPageData.setAttribute(pageType);
        }
        childPageData.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
        childPageData.setOrRemoveAttribute(PageData.PropertySUITES, suites);
        childPage.commit(childPageData);
        recentChanges.updateRecentChanges(childPage);
        
        return childPage;
    }



    private boolean nameIsInvalid(String name) {
        if (name.equals(""))
            return true;
        return !PathParser.isSingleWikiWord(name);
    }

    private boolean pageAlreadyExists(String childName) {
        return currentPage.getPageCrawler().pageExists(PathParser.parse(childName));
    }

    private void setAttributes(WikiPage childPage) {
        PageData childPageData = childPage.getData();
        if (pageTemplate != null) {
            childPageData.setProperties(pageTemplate.getData().getProperties());
        } else if (pageType.equals("Static")) {
            childPageData.getProperties().remove("Test");
            childPageData.getProperties().remove("Suite");
        } else if ("Test".equals(pageType) || "Suite".equals(pageType)) {
            childPageData.getProperties().remove("Test");
            childPageData.getProperties().remove("Suite");
            childPageData.setAttribute(pageType);
        }
        childPageData.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
        childPageData.setOrRemoveAttribute(PageData.PropertySUITES, suites);
        childPage.commit(childPageData);
    }

    private Response errorResponse(FitNesseContext context, Request request) throws Exception {
        return new ErrorResponder("Invalid Child Name").makeResponse(context, request);
    }

    private Response alreadyExistsResponse(FitNesseContext context, Request request) throws Exception {
        return new ErrorResponder("Child page already exists", 409).makeResponse(context, request);
    }

    private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
        return new NotFoundResponder().makeResponse(context, request);
    }
}

