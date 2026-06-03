const http = require('http');

const API_BASE = 'http://localhost:8081/api/v1';

// Helper function to send requests
function sendPostRequest(endpoint, body, token) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const options = {
      hostname: 'localhost',
      port: 8081,
      path: `/api/v1${endpoint}`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data),
      }
    };
    if (token) options.headers['Authorization'] = `Bearer ${token}`;

    const req = http.request(options, (res) => {
      let responseBody = '';
      res.on('data', chunk => responseBody += chunk);
      res.on('end', () => resolve({ status: res.statusCode, body: responseBody }));
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function sendGetRequest(path) {
  return new Promise((resolve, reject) => {
    const startTime = Date.now();
    const req = http.request({
      hostname: 'localhost',
      port: 8081,
      path: path,
      method: 'GET',
    }, (res) => {
      // Consume response data to free up memory
      res.on('data', () => {}); 
      res.on('end', () => resolve({ 
        status: res.statusCode, 
        timeMs: Date.now() - startTime 
      }));
    });
    req.on('error', reject);
    req.end();
  });
}

async function runLoadTest() {
  console.log('🚀 Starting ScaleLink Load Test Suite...\n');

  // 1. Register a test user
  const username = 'user_' + Date.now().toString().slice(-8);
  console.log(`👤 Registering test user: ${username}`);
  const regRes = await sendPostRequest('/auth/register', {
    username,
    email: `${username}@example.com`,
    password: 'Password123!'
  });
  
  if (regRes.status !== 201) {
    console.error('Failed to register user:', regRes.body);
    return;
  }
  const token = JSON.parse(regRes.body).data.accessToken;
  console.log('✅ User registered successfully. JWT Token acquired.\n');

  // 2. Test Rate Limiter (POST /urls limit is 10/min)
  console.log('🚦 TEST 1: URL Creation Rate Limiter (Max 10 requests/min)');
  console.log('Sending 20 concurrent POST requests to create URLs...');
  
  const postPromises = [];
  for (let i = 0; i < 20; i++) {
    postPromises.push(sendPostRequest('/urls', { originalUrl: `https://example.com/test${i}` }, token));
  }
  
  const postResults = await Promise.all(postPromises);
  const postSuccesses = postResults.filter(r => r.status === 201).length;
  const postRateLimited = postResults.filter(r => r.status === 429).length;
  
  console.log(`✅ ${postSuccesses} requests succeeded (201 Created)`);
  console.log(`⛔ ${postRateLimited} requests were blocked (429 Too Many Requests)`);
  if (postSuccesses <= 10 && postRateLimited > 0) {
    console.log('🏆 Rate Limiter is WORKING perfectly!\n');
  } else {
    console.log('❌ Rate Limiter might have failed.\n');
  }

  // Get a valid short code to test redirects
  const validPost = postResults.find(r => r.status === 201);
  const shortCode = JSON.parse(validPost.body).data.shortCode;
  
  // 3. Test Redirect Performance (GET /{shortCode})
  console.log(`⚡ TEST 2: Redis Cache Redirect Performance`);
  console.log(`Sending 1,000 concurrent GET requests to /${shortCode}...`);
  
  const getPromises = [];
  for (let i = 0; i < 1000; i++) {
    getPromises.push(sendGetRequest(`/${shortCode}`));
  }
  
  const getResults = await Promise.all(getPromises);
  const getSuccesses = getResults.filter(r => r.status === 302).length;
  
  let totalTime = 0;
  let maxTime = 0;
  getResults.forEach(r => {
    totalTime += r.timeMs;
    if (r.timeMs > maxTime) maxTime = r.timeMs;
  });
  const avgTime = totalTime / 1000;
  
  console.log(`✅ ${getSuccesses}/1000 requests successfully returned 302 Redirect`);
  console.log(`📊 Average Response Time: ${avgTime.toFixed(2)} ms`);
  console.log(`📈 Max Response Time: ${maxTime} ms`);
  
  if (avgTime < 50) {
    console.log('🏆 Redis Caching is incredibly FAST! System is highly scalable.\n');
  } else {
    console.log('⚠️ Response times are a bit high.\n');
  }
  
  console.log('🎉 Load Testing Complete!');
}

runLoadTest();
