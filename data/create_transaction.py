import json
from collections import OrderedDict

import random
import operator

file = open("brands_dictionary.txt", "r")
pre_brands = file.readlines()
file.close()

brands = []

for i in pre_brands:
    temp = i.split(",")
    brands.append(temp[1].replace("\n", ""))

data = OrderedDict()
res_list = OrderedDict()

data["bank_name"] = "우리은행"

temp = []

for i in range(0, 100) :   
    temp_year = str(2022)
    temp_month = str(random.randrange(1, 13)).zfill(2)
    temp_day = str(random.randrange(1, 32)).zfill(2)
    
    temp_hour = str(random.randrange(0, 25)).zfill(2)
    temp_min = str(random.randrange(0, 61)).zfill(2)
    temp_sec =  str(random.randrange(0, 61)).zfill(2)
    
    temp.append({
        "tran_date": str(temp_year) + str(temp_month) + str(temp_day),
        "tran_time": str(temp_hour) + str(temp_min) + str(temp_sec),
        "printed_content": brands[random.randrange(0, len(brands))],
        "tran_amt": str(random.randrange(0, 20000) * 10),
    })
    
sorted_temp = sorted(temp, key=operator.itemgetter("tran_date"))

data["res_list"] = sorted_temp

with open("transaction.json", "w", encoding="utf-8") as make_file :
    json.dump(data, make_file, ensure_ascii=False, indent="\t")