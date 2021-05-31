### Exporting Kibana Dashboard

In Kibana, go to your dashboard and copy the ID from the URL.

Then do a GET request to the export API with that ID:
```
http://localhost:5601/api/kibana/dashboards/export?dashboard=<DASHBOARD_ID>
```

Name the file `dashboard.json` and put it in this folder.