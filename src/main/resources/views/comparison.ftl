<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Home</title>
  <style>
      table, th, td {
          border: 1px solid black;
          border-collapse: collapse;
      }

      th, td {
          padding: 5px;
      }
  </style>
</head>
<body>
<form method='post' enctype='multipart/form-data' accept-charset="utf-8">
  <div>
    <div>First file:</div>
    <input type='file' name='file' accept=".csv"><br/><br/>
    <div>Second file:</div>
    <input type='file' name='file' accept=".csv"><br/><br/>
    <button>Upload csv</button>
  </div>
</form>
<#if comparison_results?? >
  <br/>
    <#list comparison_results>
      <table>
          <#items as comparison_result>
            <tr>
              <th colspan="2">${comparison_result.file_name}</th>
            </tr>
            <tr>
              <td>Total records:</td>
              <td>${comparison_result.total_records}</td>
            </tr>
            <tr>
              <td>Matching records:</td>
              <td>${comparison_result.matching_records}</td>
            </tr>
            <tr>
              <td>Unmatched records:</td>
              <td>${comparison_result.unmatched_records}</td>
            <tr>
              <td>Duplicate transaction records:</td>
              <td>${comparison_result.duplicate_transaction_records}</td>
            </tr>
          </#items>
      </table>
    </#list>
</#if>

<#--<#if unmatched_reports?? >-->
<#--  <br/><br/>-->
<#--    <#list unmatched_reports>-->
<#--      <table>-->
<#--          <#items as unmatched_report_row>-->
<#--            <tr>-->
<#--              <th colspan="2">${unmatched_report_row.file_name}</th>-->
<#--            </tr>-->
<#--            <tr>-->
<#--              <td>Date:</td>-->
<#--              <td>${unmatched_report_row.date}</td>-->
<#--            </tr>-->
<#--          </#items>-->
<#--      </table>-->
<#--    </#list>-->
<#--</#if>-->

</body>
</html>
