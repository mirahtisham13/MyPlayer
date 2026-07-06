import urllib.request
import json
req = urllib.request.Request("https://api.github.com/search/issues?q=repo:google/ExoPlayer+audio+delay")
try:
    response = urllib.request.urlopen(req)
    res = json.loads(response.read().decode("utf-8"))
    for item in res.get("items", [])[:5]:
        print(item.get("title"))
except Exception as e:
    print(e)
