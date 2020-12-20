<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>

   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hi,</p>

                                             <p>${person.properties.firstName}
                                             <#if person.properties.lastName?exists> ${person.properties.lastName}</#if>
                                             has added a new discussion post that mentions you. <#if contextNodeRef.siteShortName??>The post is in the ${contextNodeRef.siteShortName!""} site</#if> in reply to "${document.parent.properties['cm:title']}".

                                             <p>The post is:<br />
                                             ${document.content}</p>

                                             <p>Click this link to view the discussion:<br />
                                             <br />
                                             <#if contextNodeRef.isContainer>
                                                 <#if contextNodeRef.siteShortName??>
                                                    <a href="${shareUrl}/page/site/${contextNodeRef.siteShortName}/discussions-topicview?topicId=${document.parent.name}">${shareUrl}/page/site/${contextNodeRef.siteShortName}/discussions-topicview?topicId=${document.parent.name}</a>
                                                 <#else>
                                                    <a href="${shareUrl}/page/folder-details?nodeRef=${contextNodeRef.nodeRef}">${shareUrl}/page/folder-details?nodeRef=${contextNodeRef.nodeRef}</a>
                                                 </#if>
                                             <#else>
                                                 <a href="${contextNodeRef.shareUrl}">${contextNodeRef.shareUrl}</a>
                                             </#if></p>

                                             <p>Sincerely,<br />
                                             Alfresco</p>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>