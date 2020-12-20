package com.metaversant.alfresco.mentions.service;

import com.metaversant.alfresco.mentions.behavior.ScanCommentForMention;
import com.metaversant.alfresco.mentions.exceptions.MentionNotifierException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for notifying a list of users about a new comment node in which they are mentioned.
 *
 * Created by jpotts, Metaversant on 7/31/17.
 */
public class MentionNotifier {

    // Dependencies
    private ActionService actionService;
    private NodeService nodeService;
    private SearchService searchService;
    private PersonService personService;

    private Logger logger = Logger.getLogger(MentionNotifier.class);

    private static final String COMMENT_NOTIFICATION_SUBJECT = "PMGA DMS: Comment Notification";
    private static final String COMMENT_NOTIFICATION_TEMPLATE_PATH = "/app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_comment_email.html.ftl";
    private static final String POST_NOTIFICATION_SUBJECT = "PMGA DMS: Discussion Notification";
    private static final String POST_NOTIFICATION_TEMPLATE_PATH = "/app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_post_email.html.ftl";
    private static final QName TYPE_LINK = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "link");

    public void notifyMentionedUsers(NodeRef nodeRef, List<String> userNameList, int type) throws MentionNotifierException {
        for (String userName : userNameList) {
            sendNotification(userName, nodeRef, type);
        }
    }

    private void sendNotification(String userName, NodeRef nodeRef, int type) throws MentionNotifierException {
        String recipient = getEmailAddress(userName);
        if (recipient == null) {
            logger.debug("Could not determine recipient email address from userName: " + userName);
        }
        logger.debug("Sending notification to: " + recipient + " for: " + nodeRef);

        // Get the template to use for the notification
        NodeRef notificationTemplate = getNotificationTemplate(type);

        // Get the node this comment is attached to
        NodeRef contextNodeRef = getContextNodeRef(nodeRef);

        if (TYPE_LINK.equals(nodeService.getType(contextNodeRef))) {
            // No support for links at the moment
            logger.debug("Context node is a link, skipping");
            return;
        }

        // Set up a map of additional template params to be used for rendering the message
        Map<String, Serializable> templateParams = getTemplateContext(nodeRef, contextNodeRef);

        // Use action service to invoke the mail action executer
        Action mailAction = actionService.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, recipient);
        if (type == ScanCommentForMention.COMMENT_MENTION) {
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, COMMENT_NOTIFICATION_SUBJECT);
        } else if (type == ScanCommentForMention.POST_MENTION) {
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, POST_NOTIFICATION_SUBJECT);
        }
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, notificationTemplate);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateParams);
        mailAction.setExecuteAsynchronously(true);
        actionService.executeAction(mailAction, nodeRef);

        logger.debug("Leaving sendNotification");
    }

    private NodeRef getContextNodeRef(NodeRef commentNodeRef) {
        NodeRef commentParent = nodeService.getPrimaryParent(commentNodeRef).getParentRef();
        NodeRef discussionNodeRef = nodeService.getPrimaryParent(commentParent).getParentRef();
        return nodeService.getPrimaryParent(discussionNodeRef).getParentRef();
    }

    private Map<String, Serializable> getTemplateContext(NodeRef nodeRef, NodeRef contextNodeRef) {
        Map<String, Serializable> templateParams = new HashMap<String, Serializable>();
        templateParams.put("contextNodeRef", contextNodeRef);
        return templateParams;
    }

    private NodeRef getNotificationTemplate(int type) throws MentionNotifierException {
        String query;
        if (type == ScanCommentForMention.COMMENT_MENTION) {
            query = "PATH:\"" + COMMENT_NOTIFICATION_TEMPLATE_PATH + "\"";
        } else if (type == ScanCommentForMention.POST_MENTION) {
            query = "PATH:\"" + POST_NOTIFICATION_TEMPLATE_PATH + "\"";
        } else {
            throw new MentionNotifierException("Unexpected notification type specified: " + type);
        }

        ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                SearchService.LANGUAGE_FTS_ALFRESCO,
                query
        );

        if (resultSet.length() == 0) {
            throw new MentionNotifierException("Could not fetch notification template");
        }

        if (resultSet.length() > 1) {
            logger.warn("Found more than one matching notification template");
        }

        return resultSet.getNodeRef(0);
    }

    private String getEmailAddress(String userName) {
        NodeRef personNodeRef = personService.getPerson(userName);

        if (personNodeRef != null) {
            String emailAddress = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
            if (StringUtils.isNotBlank(emailAddress)) {
                return emailAddress;
            }
        }

        return null;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
