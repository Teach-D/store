# from locust import HttpUser, task, between
# import random
#
# class CouponLoadTestUser(HttpUser):
#     wait_time = between(1, 3)  # 각 요청 사이 대기 시간 (1~3초 랜덤)
#
#     def on_start(self):
#         # 1부터 10,000,000 사이의 사용자 ID를 미리 생성
#         self.user_ids = list(range(1, 10_000_001))
#
#     @task
#     def issue_coupon(self):
#         member_id = random.choice(self.user_ids)  # 랜덤한 사용자 ID 선택
#
#         with self.rest("POST", f"/coupons/load-test1/issue{member_id}"):
#             pass
import random
from locust import task, FastHttpUser, stats

stats.PERCENTILES_TO_CHART = [0.95, 0.99]

class CouponIssueV1(FastHttpUser):
    connection_timeout = 10.0
    network_timeout = 10.0

    @task
    def issue(self):
        userId = random.randint(1, 10_000_000)  # 올바른 변수 할당

        with self.rest("POST", f"/coupons/load-test/1/issue/{userId}"):
            pass