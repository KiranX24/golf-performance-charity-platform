const API =
  import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export async function api(path, options = {}) {
  const token = localStorage.getItem('dh_token');

  const headers = new Headers(options.headers || {});

  if (
    !(options.body instanceof FormData) &&
    options.body !== undefined
  ) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response;

  try {
    response = await fetch(`${API}${path}`, {
      ...options,
      headers
    });
  } catch {
    throw new Error(
      'Unable to connect to the backend. Please make sure the Spring Boot server is running.'
    );
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();

  let data = null;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('dh_token');
      localStorage.removeItem('dh_user');

      window.dispatchEvent(
        new Event('dh:logout')
      );
    }

    const message =
      data?.message ||
      (
        response.status === 403
          ? 'Access denied.'
          : `Request failed (${response.status})`
      );

    throw new Error(message);
  }

  return data;
};


// =========================
// AUTH
// =========================

export const authApi = {
  login: body =>
    api('/auth/login', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  register: body =>
    api('/auth/register', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  me: () =>
    api('/auth/me')
};


// =========================
// USER
// =========================

export const userApi = {
  dashboard: () =>
    api('/dashboard')
};


// =========================
// PLANS
// =========================

export const plansApi = {

  // User-facing active plans
  list: () =>
    api('/plans'),

  // Admin: all plans including inactive plans
  adminList: () =>
    api('/admin/plans'),

  // Admin: create plan
  create: body =>
    api('/admin/plans', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  // Admin: update plan
  update: (id, body) =>
    api(`/admin/plans/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),

  // Admin: activate / deactivate plan
  setActive: (id, value) =>
    api(
      `/admin/plans/${id}/active?value=${value}`,
      {
        method: 'PATCH'
      }
    )
};


// =========================
// SUBSCRIPTIONS
// =========================

export const subscriptionsApi = {

  // Get current user's subscription history
  mine: () =>
    api('/subscriptions/me'),

  // Create Stripe Checkout Session
  checkout: planId =>
    api(`/subscriptions/checkout/${planId}`, {
      method: 'POST'
    }),

  // Direct activation
  // Keep only for backend testing/internal use.
  // User-facing Plans page should use checkout().
  activate: planId =>
    api(`/subscriptions/activate/${planId}`, {
      method: 'POST'
    }),

  // Cancel subscription
  cancel: () =>
    api('/subscriptions/cancel', {
      method: 'POST'
    })
};


// =========================
// SCORES
// =========================

export const scoresApi = {
  list: () =>
    api('/scores'),

  create: body =>
    api('/scores', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  update: (id, body) =>
    api(`/scores/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),

  remove: id =>
    api(`/scores/${id}`, {
      method: 'DELETE'
    })
};


// =========================
// CHARITIES
// =========================

export const charitiesApi = {

  // User-facing charities
  list: (q = '') =>
    api(
      `/charities${
        q
          ? `?q=${encodeURIComponent(q)}`
          : ''
      }`
    ),

  get: id =>
    api(`/charities/${id}`),

  selection: () =>
    api('/charities/me/selection'),

  select: body =>
    api('/charities/me/selection', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  donate: body =>
    api('/charities/donations', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  // Admin: all charities including archived charities
  adminList: () =>
    api('/admin/charities'),

  // Admin: create charity
  adminCreate: body =>
    api('/admin/charities', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  // Admin: update charity
  adminUpdate: (id, body) =>
    api(`/admin/charities/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),

  // Admin: archive / unarchive charity
  setArchived: (id, value) =>
    api(
      `/admin/charities/${id}/archived?value=${value}`,
      {
        method: 'PATCH'
      }
    )
};


// =========================
// DRAWS
// =========================

export const drawsApi = {
  list: () =>
    api('/draws'),

  get: id =>
    api(`/draws/${id}`),

  simulate: body =>
    api('/admin/draws/simulate', {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  publish: id =>
    api(`/admin/draws/${id}/publish`, {
      method: 'POST'
    })
};


// =========================
// WINNERS
// =========================

export const winnersApi = {
  // User: my winners
  mine: () =>
    api('/winners/me'),

  // User: upload winner proof
  proof: (id, formData) =>
    api(`/winners/${id}/proof`, {
      method: 'POST',
      body: formData
    }),

  // Admin: all winners
  all: () =>
    api('/admin/winners'),

  // Admin: approve / reject winner
  verify: (id, body) =>
    api(`/admin/winners/${id}/verify`, {
      method: 'POST',
      body: JSON.stringify(body)
    }),

  // Admin: mark payout as paid
  markPaid: payoutId =>
    api(`/admin/payouts/${payoutId}/paid`, {
      method: 'POST'
    })
};

// =========================
// ADMIN
// =========================

export const adminApi = {

  stats: () =>
    api('/admin/stats'),

  users: () =>
    api('/admin/users'),

  setUserActive: (id, value) =>
    api(
      `/admin/users/${id}/active?value=${value}`,
      {
        method: 'PATCH'
      }
    )
};