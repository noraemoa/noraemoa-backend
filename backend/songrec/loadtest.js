import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

const prompts = [
  "우울한 밤에 듣기 좋은 노래",
  "설레는 봄날 어울리는 노래",
  "공부할 때 듣는 노래",
  "헬스할 때 듣는 노래",
  "몽환적인 느낌의 노래",
  "청량한 느낌의 노래",
];

export const options = {
  stages: [
    { duration: '10s', target: 100 }, // 램프업
    { duration: '30s', target: 100 }, // 유지
    { duration: '10s', target: 0 },   // 램프다운
  ],
};

// 테스트 시작 시 한 번만 실행 — 로그인해서 토큰 확보
export function setup() {
  const loginRes = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ email: 'qqq@qqq.com', password: 'qqq' }),
      { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status !== 200 && loginRes.status !== 201) {
    throw new Error(`로그인 실패: status=${loginRes.status}, body=${loginRes.body}`);
  }

  const token = loginRes.json('accessToken');
  if (!token) {
    throw new Error(`accessToken 없음: ${loginRes.body}`);
  }

  return { token };
}

// 각 가상 유저(VU)가 반복 실행하는 부분
export default function (data) {
  const prompt = prompts[Math.floor(Math.random() * prompts.length)];

  const res = http.post(
      `${BASE_URL}/requests`,
      JSON.stringify({ prompt }),
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${data.token}`,
        },
      }
  );

  check(res, {
    'status is 2xx': (r) => r.status >= 200 && r.status < 300,
    'not 401/403 (인증 문제 아님)': (r) => r.status !== 401 && r.status !== 403,
  });
}