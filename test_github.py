import urllib.request
import json
req = urllib.request.Request("https://api.github.com/search/issues?q=repo:google/ExoPlayer+%22audio+delay%22+OR+%22audio+offset%22")
try:
    response = urllib.request.urlopen(req)
    res = json.loads(response.read().decode("utf-8"))
    for item in res.get("items", [])[:3]:
        print(item.get("title"), item.get("html_url"))
except Exception as e:
    print(e)
