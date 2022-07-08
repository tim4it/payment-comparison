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
  <table style="background-color: #ebebff">
    <tr style="background-color: #000000">
      <th colspan="2" style="text-align: left; color: white; font-size:12pt; white-space: nowrap">
        Payment comparison
      </th>
    </tr>
    <tr>
      <td>First file</td>
      <td><input type='file' name='file' accept=".csv"></td>
    </tr>
    <tr>
      <td>Second file</td>
      <td><input type='file' name='file' accept=".csv"></td>
    </tr>
    <tr>
      <td colspan="2" style="text-align: right">
        <button>Upload csv</button>
      </td>
    </tr>
  </table>
</form>
<#if comparison_reports?? >
    <#assign firstReport = comparison_reports[0]>
    <#assign secondReport = comparison_reports[1]>
  <br/>
  <table>
    <tr style="background-color: #000000">
      <th></th>
      <th style="color: white; font-size:10pt; white-space: nowrap">${firstReport.file_name}</th>
      <th style="color: white; font-size:10pt; white-space: nowrap">${secondReport.file_name}</th>
    </tr>
    <tr style="background-color:#eeeeee">
      <td>Total records</td>
      <td>${firstReport.total_records}</td>
      <td>${secondReport.total_records}</td>
    </tr>
    <tr style="background-color:#dddddd">
      <td>Matching records</td>
      <td>${firstReport.matching_records}</td>
      <td>${secondReport.matching_records}</td>
    </tr>
    <tr style="background-color:#eeeeee">
      <td>Unmatched records</td>
      <td>${firstReport.unmatched_records}</td>
      <td>${secondReport.unmatched_records}</td>
    <tr style="background-color:#dddddd">
      <td>Duplicate transaction group records</td>
      <td>${firstReport.duplicate_transaction_group_records}</td>
      <td>${secondReport.duplicate_transaction_group_records}</td>
    </tr>
    <tr style="background-color:#eeeeee">
      <td>Duplicate transaction records</td>
      <td>${firstReport.duplicate_transaction_records}</td>
      <td>${secondReport.duplicate_transaction_records}</td>
    </tr>
  </table>
</#if>
<#if unmatched_reports?? >
  <br/>
  <table>
    <tr style="background-color: #000000">
      <th style="color: white; font-size:10pt; white-space: nowrap">File name</th>
      <th style="color: white; font-size:10pt; white-space: nowrap">Date</th>
      <th style="color: white; font-size:10pt; white-space: nowrap">Transaction amount</th>
      <th style="color: white; font-size:10pt; white-space: nowrap">Transaction id</th>
      <th style="color: white; font-size:10pt; white-space: nowrap">Wallet reference</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #464646">File name</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #464646">Date</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #464646">Transaction amount</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #464646">Transaction id</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #464646">Wallet reference</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #606060">File name</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #606060">Date</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #606060">Transaction amount</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #606060">Transaction id</th>
      <th style="color: white; font-size:10pt; white-space: nowrap; background-color: #606060">Wallet reference</th>
    </tr>
      <#list unmatched_reports>
          <#items as unmatched_report>
            <tr style="background-color: ${((unmatched_report_index % 2)==0)?string("#dddddd", "#eeeeee")}">
              <td style="white-space: nowrap">
                <b><i>${unmatched_report.file_name_1}</i></b></td>
              <td style="white-space: nowrap">
                  ${unmatched_report.date_1}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_amount_1}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_id_1}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.wallet_reference_1}</td>
              <td style="white-space: nowrap">
                <b><i>${unmatched_report.file_name_2}</i></b></td>
              <td style="white-space: nowrap">
                  ${unmatched_report.date_2}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_amount_2}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_id_2}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.wallet_reference_2}</td>
              <td style="white-space: nowrap">
                <b><i>${unmatched_report.file_name_3}</i></b></td>
              <td style="white-space: nowrap">
                  ${unmatched_report.date_3}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_amount_3}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.transaction_id_3}</td>
              <td style="white-space: nowrap">
                  ${unmatched_report.wallet_reference_3}</td>
            </tr>
          </#items>
      </#list>
  </table>
</#if>
</body>
</html>
