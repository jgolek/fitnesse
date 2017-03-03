package vertx;


import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTES;

import java.util.Map;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageFactory;
import fitnesse.html.template.PageTitle;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.TemplateUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class NewPageResponder2 {
  public static final String DEFAULT_PAGE_CONTENT_PROPERTY = "newpage.default.content";
  public static final String DEFAULT_PAGE_CONTENT = "!contents -R2 -g -p -f -h";

  public static final String PAGE_TEMPLATE = "pageTemplate";
  public static final String PAGE_TYPE = "pageType";
  public static final String PAGE_TYPES = "pageTypes";

  public String makeHtml(PageFactory pageFactory, String qualifiedName, PageCrawler crawler, Map<String, String> params) {
    HtmlPage html = pageFactory.newPage();
    html.setTitle("New page");

    html.setPageTitle(new PageTitle("New Page", PathParser.parse(qualifiedName)));
    html.setMainTemplate("editPage");
    
    html.put("resource", qualifiedName);

    html.put("isNewPage", true);
    html.put(EditResponder.HELP_TEXT, "");

    WikiPage parentWikiPage = crawler.getPage(PathParser.parse(qualifiedName));
    html.put(EditResponder.TEMPLATE_MAP, TemplateUtil.getTemplateMap(parentWikiPage));
    if (params.containsKey(PAGE_TEMPLATE)) {
      String pageTemplate = params.get(PAGE_TEMPLATE);
      WikiPage template = crawler.getPage(PathParser.parse(pageTemplate));
      html.put(EditResponder.CONTENT_INPUT_NAME, template.getData().getContent());
      html.put(EditResponder.PAGE_TYPE, PageType.fromWikiPage(template));
      html.put(PAGE_TEMPLATE, pageTemplate);
    } else if (params.containsKey(PAGE_TYPE)) {
      String pageType = params.get(PAGE_TYPE);
      // Validate page type:
      PageType.fromString(pageType);
      html.put(EditResponder.PAGE_TYPE, pageType);
      html.put(EditResponder.CONTENT_INPUT_NAME, getDefaultContent(parentWikiPage));
    } else {
      html.put(PAGE_TYPES, PAGE_TYPE_ATTRIBUTES);
      html.put(EditResponder.CONTENT_INPUT_NAME, getDefaultContent(parentWikiPage));
    }
    return html.html();
  }


  public static String getDefaultContent(WikiPage page) {
    String content = page.getVariable(DEFAULT_PAGE_CONTENT_PROPERTY);
    if (content == null) {
      content = DEFAULT_PAGE_CONTENT;
    }
    return content;
  }



  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
