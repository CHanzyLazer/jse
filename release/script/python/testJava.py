# 测试在 python 中调用 java（通过 jep）
from java.util import ArrayList, HashMap

a = ArrayList()
a.add("abc")
a += ["def"]
print(a)

m = HashMap()
m.put("listkey", a)
m["otherkey"] = "xyz"
print(m)

