import pymysql
import pandas as pd
import json
import random
import operator
from flask import Flask, jsonify, request
from sklearn.metrics.pairwise import cosine_similarity
from flask_restful import Api, Resource
from collections import OrderedDict

# example = '''SELECT * FROM cardvisor_beta3.serviceOne;'''

# DB 연결
def db_connector(sql):
    db = pymysql.connect(
        host='127.0.0.1',
        port=3307,
        user='root',
        passwd='root',
        db='cardvisor_beta3',
        charset='utf8',
        autocommit=True,
        cursorclass=pymysql.cursors.DictCursor
    )
    cursor = db.cursor()
    cursor.execute(sql)
    result = cursor.fetchall()
    db.close()
    return result

# 두 리스트에서 중복되지 않는 elememnt 추출 (사용자가 브랜드를 선택했지만 해당 브랜드 혜택을 가지고있는 카드가 존재하지 않을 경우)
def non_match_elements(list_a, list_b):
    non_match = []
    for i in list_a:
        if i not in list_b:
            non_match.append(i)
    return non_match


app = Flask(__name__)
api = Api(app)


# @api.route('/motion/<string:word>', methods=['GET'])
# class motion(Resource):
#     def get(self, word):

#         word = unquote_plus(word)
#         combined = join_jamos(word)

#         return {'result' : "%s" % combined}



class serviceOne(Resource):
    def get(self):
        sql = '''SELECT * FROM cardvisor_beta3.serviceone;'''
        result = db_connector(sql)
        df = pd.DataFrame(result)

        # dataframe에서 'serviceone_id' 제거
        df = df.drop(columns=['serviceone_id'])

        # 'brand_id'값을 OneHot Encoding 하여 각 id를 칼럼화
        brand_dummies = pd.get_dummies(df.brand_id)
        df = pd.concat([df, brand_dummies], axis = 'columns')

        # 선택된 브랜드 아이들을 따로 저장
        brands1 = df.brand_id

        # OneHot Encoding을 마쳤으니 'brand_id' 칼럼 제거
        df = df.drop(columns=['brand_id'])

        # 'member_id' 칼럼의 값이 전부 1 이므로 해당 칼럼의 value에 맞춰 통일 => 이때 각 'brand_id' 칼럼값들은 sum()
        # 결과 => member_id : 1, brand_id1 : 1, brand_id2 : 1 , ..... , brand_id(n) : 1
        df = df.groupby(['member_id'], as_index=False).sum()

        # 유사도 계산에 활용될 사용자가 선택한 브랜드값들에 1을 주입한 dataframe 출력 (터미널 확인하기)
        print(df)

        # 브랜드에 대한 혜택이 존재하지 않는 카드가 있을 수 있으니 아직 완성된것은 아니므로 members_choice 변수로 저장해놓음
        members_choice = df.copy()


        # 6000개의 혜택중에 사용자가 선택한 'brand_id'값을 가지고 있는 혜택에서 'card_code'와 'brand_id' 추출
        sql = f"""
        SELECT card_code, brand_id FROM cardvisor_beta3.benefit
        WHERE brand_id in {tuple(brands1)}
        """
        options = db_connector(sql)
        df = pd.DataFrame(options)

        # 추출된 값 확인해보기
        print(df)

        # 사용자가 선택한 'brand_id' 중에서 해당 유효성 있는?(1개의 카드라도 해당 혜택 포함) 'brand_id' 값들을 brands2 변수에 저장
        brands2 = df.brand_id

        # brands1 과 brands2를 비교하여 혜택이 존재하지 않는 'brand_id'를 추출
        trash = non_match_elements(list(brands1), list(brands2))

        # 추출된 'brand_id' 값 칼럼을 members_choice에서 제거
        for col in trash:
            members_choice = members_choice.drop(columns=[col])

        # 결과 확인
        print(members_choice)


        # 'card_code', 'brand_id' dataframe에서 'brand_id' value들을 OneHot Encoding 하여 칼럼화
        brand_dummies = pd.get_dummies(df.brand_id)
        df = pd.concat([df, brand_dummies], axis = 'columns')

        # 'brand_id' 칼럼 제거
        df = df.drop(columns=['brand_id'])

        # 'member_id' 칼럼의 값이 전부 1 이므로 해당 칼럼의 value에 맞춰 통일 => 이때 각 'brand_id' 칼럼값들은 sum()
        # 하지만 이때 'card_code'와 'brand' 값이 완전히 중복되는 row들이 있으므로 혜택이 없으면 0이지만 있으면 무조건 1이 아님
        df = df.groupby(['card_code'], as_index=False).sum()

        # int -> bool -> int datatype 변환을 거쳐 숫자가 존재하면 전부 1로 교체
        temp = df.loc[:, df.columns != 'card_code'].astype('bool')
        temp = temp.loc[:, temp.columns != 'card_code'].astype('int')
        df = pd.concat([df['card_code'], temp], axis='columns')

        # 유사도 계산에 활용될 'card_code', 와 각 브랜드 아이디값 칼럼 dataframe 출력
        print(df)
        recommendable_cards = df.copy()


        # 코사인 유사도 계산
        final = pd.DataFrame(cosine_similarity(
            members_choice.loc[:, members_choice.columns != 'member_id'],
            recommendable_cards.loc[:, recommendable_cards.columns != 'card_code']
        ),
            columns = list(recommendable_cards.card_code), index = ['similarity']
        )

        #칼럼이 데이터를 더 가공하기 쉬우므로 transpose
        final = final.transpose()

        # 전체 유사도 출력
        print(final)


        final = final.sort_values(by=['similarity'], ascending=False)
        final = final.head(10)

        # 유사도 높은 상위 10개 항목 출력
        print(final)

        # 유사도 높은 상위 10개 항목의 'card_code' 값을 리스트로 저장 후 출력
        final_cards = list(final.index.values)
        final_cards = list([int(x) for x in final_cards])
        print(final_cards)

        cardList = { "cards" : final_cards }


        # 해당 리스트를 브라우저 화면에 출력
        print(cardList)

        sql = '''truncate table cardvisor_beta3.serviceone;'''
        result = db_connector(sql)

        return jsonify(cardList)

class serviceTwoSave(Resource):
    def get(self):
        file = open("brands_dictionary.txt", "r", encoding="UTF-8")
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

        # with open("transaction.json", "w", encoding="utf-8") as make_file :
        #     json.dump(data, make_file, ensure_ascii=False, indent="\t")
        # print(data)

        return data


class serviceTwoRecommend(Resource):
    def get(self):
        sql = '''
            SELECT * FROM cardvisor_beta3.servicetwo;
            '''
        result = db_connector(sql)
        df = pd.DataFrame(result)
        brands = df["brand_id"]

        categories = []

        for brand in brands:
            sql = "select category_id from brand where brand_id = {}".format(brand)
            result = db_connector(sql)
            categories.append(result[0].get('category_id'))

        df['category_id'] = categories

        member_table = df.copy()

        member_table = member_table.drop(columns = ['brand_id'])
        member_table = member_table.drop(columns = ['servicetwo_id'])

        category_dummies = pd.get_dummies(member_table.category_id)
        member_table = pd.concat([member_table, category_dummies], axis = 'columns')

        member_table = member_table.groupby(['category_id', 'member_id'], as_index=False).sum()

        member_table = member_table.drop(columns = ['category_id'])

        consumption = member_table.loc[:, (member_table.columns != 'member_id') & (member_table.columns != 'cost')].astype('bool')
        consumption = consumption.loc[:, (consumption.columns != 'member_id') & (consumption.columns != 'cost')].astype('int')

        consumption_total = pd.concat([member_table['member_id'], consumption], axis='columns')

        total_consumption = member_table['cost'].sum()

        consumption_total = consumption_total.groupby(['member_id'], as_index=False).sum()

        memberId = consumption_total['member_id']
        consumption_total = consumption_total.drop(columns = ['member_id'])
        consumption_total_index = consumption_total.transpose().index.values

        cost_list = member_table['cost']
        cost_list = cost_list.set_axis(consumption_total_index, axis='index')
        cost_list = cost_list.mul(1/total_consumption)

        multiplicationResults = consumption_total.mul(cost_list);

        final_member_table = pd.concat([memberId, multiplicationResults], axis='columns')


        sql = f"""
            SELECT card_code, category_id FROM cardvisor_beta3.benefit
            WHERE brand_id in {tuple(brands)}
            """
        options = db_connector(sql)
        df = pd.DataFrame(options)

        categories2 = df.category_id

        trash = non_match_elements(list(set(categories)), list(categories2))

        # 추출된 'brand_id' 값 칼럼을 members_choice에서 제거
        for col in trash:
            final_member_table = final_member_table.drop(columns=[col])

        category_dummies = pd.get_dummies(df.category_id)
        card_table = pd.concat([df, category_dummies], axis = 'columns')

        card_table = card_table.drop(columns = ['category_id'])

        card_table = card_table.groupby(['card_code'], as_index=False).sum()

        card_table_temp = card_table.drop(columns = ['card_code'])
        card_table_temp = card_table_temp.transpose()

        weight_list = []
        for i in range(len(card_table.index.values)):
            weight_list.append(card_table_temp[i].sum())

        for i in range(len(card_table.index.values)):
            card_table_temp[i] = card_table_temp[i].mul(1/weight_list[i])

        card_table_temp2 = card_table_temp.transpose()

        card_table = pd.concat([card_table['card_code'], card_table_temp2], axis='columns')

        final = pd.DataFrame(cosine_similarity(
            final_member_table.loc[:, final_member_table.columns != 'member_id'],
            card_table.loc[:, card_table.columns != 'card_code']
        ),
            columns = list(card_table.card_code), index = ['similarity']
        )

        final = final.transpose()
        final = final.sort_values(by=['similarity'], ascending=False)
        final = final.head(10)

        final_cards = list(final.index.values)
        final_cards = list([int(x) for x in final_cards])
        print(final_cards)

        cardList = { "cards" : final_cards }


        # 해당 리스트를 브라우저 화면에 출력
        print(cardList)

        # sql = '''truncate table cardvisor_beta3.servicetwo;'''
        # result = db_connector(sql)

        return jsonify(cardList)



class serviceThreeSave(Resource):
    def get(self):
        with open('transactions.json', 'r', encoding="UTF-8") as json_file:
            json_data = json.load(json_file)
    
        return json_data;


class serviceThreeRecommend(Resource):
    def get(self):
        sql = '''SELECT member_id, brand_id FROM cardvisor_beta3.servicethree;'''
        result = db_connector(sql)
        df = pd.DataFrame(result)


        # 'brand_id'값을 OneHot Encoding 하여 각 id를 칼럼화
        brand_dummies = pd.get_dummies(df.brand_id)
        df = pd.concat([df, brand_dummies], axis = 'columns')

        # 선택된 브랜드 아이들을 따로 저장
        brands1 = df.brand_id

        # OneHot Encoding을 마쳤으니 'brand_id' 칼럼 제거
        df = df.drop(columns=['brand_id'])

        # 'member_id' 칼럼의 값이 전부 1 이므로 해당 칼럼의 value에 맞춰 통일 => 이때 각 'brand_id' 칼럼값들은 sum()
        # 결과 => member_id : 1, brand_id1 : 1, brand_id2 : 1 , ..... , brand_id(n) : 1
        df = df.groupby(['member_id'], as_index=False).sum()

        # 유사도 계산에 활용될 사용자가 선택한 브랜드값들에 1을 주입한 dataframe 출력 (터미널 확인하기)
        print(df)

        # 브랜드에 대한 혜택이 존재하지 않는 카드가 있을 수 있으니 아직 완성된것은 아니므로 members_choice 변수로 저장해놓음
        members_choice = df.copy()


        # 6000개의 혜택중에 사용자가 선택한 'brand_id'값을 가지고 있는 혜택에서 'card_code'와 'brand_id' 추출
        sql = f"""
        SELECT card_code, brand_id FROM cardvisor_beta3.benefit
        WHERE brand_id in {tuple(brands1)}
        """
        options = db_connector(sql)
        df = pd.DataFrame(options)

        # 추출된 값 확인해보기
        print(df)

        # 사용자가 선택한 'brand_id' 중에서 해당 유효성 있는?(1개의 카드라도 해당 혜택 포함) 'brand_id' 값들을 brands2 변수에 저장
        brands2 = df.brand_id

        # brands1 과 brands2를 비교하여 혜택이 존재하지 않는 'brand_id'를 추출
        trash = non_match_elements(list(brands1), list(brands2))

        # 추출된 'brand_id' 값 칼럼을 members_choice에서 제거
        for col in trash:
            members_choice = members_choice.drop(columns=[col])

        # 결과 확인
        print(members_choice)


        # 'card_code', 'brand_id' dataframe에서 'brand_id' value들을 OneHot Encoding 하여 칼럼화
        brand_dummies = pd.get_dummies(df.brand_id)
        df = pd.concat([df, brand_dummies], axis = 'columns')

        # 'brand_id' 칼럼 제거
        df = df.drop(columns=['brand_id'])

        # 'member_id' 칼럼의 값이 전부 1 이므로 해당 칼럼의 value에 맞춰 통일 => 이때 각 'brand_id' 칼럼값들은 sum()
        # 하지만 이때 'card_code'와 'brand' 값이 완전히 중복되는 row들이 있으므로 혜택이 없으면 0이지만 있으면 무조건 1이 아님
        df = df.groupby(['card_code'], as_index=False).sum()

        # int -> bool -> int datatype 변환을 거쳐 숫자가 존재하면 전부 1로 교체
        temp = df.loc[:, df.columns != 'card_code'].astype('bool')
        temp = temp.loc[:, temp.columns != 'card_code'].astype('int')
        df = pd.concat([df['card_code'], temp], axis='columns')

        # 유사도 계산에 활용될 'card_code', 와 각 브랜드 아이디값 칼럼 dataframe 출력
        print(df)
        recommendable_cards = df.copy()


        # 코사인 유사도 계산
        final = pd.DataFrame(cosine_similarity(
            members_choice.loc[:, members_choice.columns != 'member_id'],
            recommendable_cards.loc[:, recommendable_cards.columns != 'card_code']
        ),
            columns = list(recommendable_cards.card_code), index = ['similarity']
        )

        #칼럼이 데이터를 더 가공하기 쉬우므로 transpose
        final = final.transpose()

        # 전체 유사도 출력
        print(final)


        final = final.sort_values(by=['similarity'], ascending=False)
        final = final.head(10)

        # 유사도 높은 상위 10개 항목 출력
        print(final)

        # 유사도 높은 상위 10개 항목의 'card_code' 값을 리스트로 저장 후 출력
        final_cards = list(final.index.values)
        final_cards = list([int(x) for x in final_cards])
        print(final_cards)

        cardList = { "cards" : final_cards }


        # 해당 리스트를 브라우저 화면에 출력
        print(cardList)

        sql = '''truncate table cardvisor_beta3.servicethree;'''
        result = db_connector(sql)

        return jsonify(cardList)
        



api.add_resource(serviceOne, "/serviceOne")
api.add_resource(serviceTwoSave,"/serviceTwo/save")
api.add_resource(serviceTwoRecommend, "/serviceTwo/recommend")
api.add_resource(serviceThreeSave,"/serviceThree/save")
api.add_resource(serviceThreeRecommend, "/serviceThree/recommend")


if __name__ == "__main__":
    app.run(host='127.0.0.1', port="5001", debug=True)