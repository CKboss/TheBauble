### Redis 存储说明:
1. stopword: 一个存停用词的set
2. len@field_reportXXX : 文档长度
3. avglen fieldXX : 所有文档的平均长度
4. word@field {doc} : 单词在文档中出现的次数统计
5. docnum 文档的数量


```redis
hget year@report_text report45
hget len@report_text report76
hget avglen report_text
```
