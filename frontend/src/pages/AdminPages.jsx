import React, { useEffect, useState } from 'react';

import {
  adminApi,
  charitiesApi,
  drawsApi,
  plansApi,
  winnersApi
} from '../api/client';

import {
  Badge,
  Button,
  Card,
  Empty,
  ErrorBox,
  Modal,
  Page,
  Spinner,
  StatCard,
  SuccessBox,
  formatDate,
  formatMoney,
  statusTone
} from '../components/UI';


/* =========================================================
   ADMIN OVERVIEW
========================================================= */

export function AdminOverview() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadStats() {
      try {
        const data = await adminApi.stats();
        setStats(data);
      } catch (e) {
        setError(e.message);
      }
    }

    loadStats();
  }, []);

  if (!stats) {
    return (
      <Page title="Admin overview">
        <ErrorBox message={error} />
        <Spinner />
      </Page>
    );
  }

  return (
    <Page
      title="Admin overview"
      subtitle="Operational control center for A Golf Performance and Charity Draw Platform."
    >
      <div className="stats-grid">

        <StatCard
          label="Users"
          value={stats.users ?? 0}
        />

        <StatCard
          label="Active subscriptions"
          value={stats.activeSubscriptions ?? 0}
        />

        <StatCard
          label="Scores"
          value={stats.scores ?? 0}
        />

        <StatCard
          label="Charities"
          value={stats.charities ?? 0}
        />

        <StatCard
          label="Draws"
          value={stats.draws ?? 0}
        />

        <StatCard
          label="Winners"
          value={stats.winners ?? 0}
        />

        <StatCard
          label="Pending verification"
          value={stats.pendingVerifications ?? 0}
        />

      </div>
	  <Card
	    className="admin-note"
	    style={{
	      marginTop: '24px'
	    }}
	  >
	    <span className="eyebrow">PLATFORM OVERVIEW</span>

	    <h3>Golf, Giving & Rewards</h3>

	    <p>
	      Manage users, subscriptions, golf scores, charity contributions,
	      draws and winner verification from one central dashboard.
	    </p>
	  </Card>
    </Page>
  );
}


/* =========================================================
   ADMIN DRAWS
========================================================= */

export function AdminDraws() {
  const [draws, setDraws] = useState([]);

  const [period, setPeriod] = useState(
    new Date().toISOString().slice(0, 10)
  );

  const [mode, setMode] = useState('RANDOM');

  const [error, setError] = useState('');
  const [ok, setOk] = useState('');
  const [loading, setLoading] = useState(false);

  const load = async () => {
    try {
      const data = await drawsApi.list();
      setDraws(data);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  async function simulate() {
    setLoading(true);
    setError('');
    setOk('');

    try {
      await drawsApi.simulate({
        drawPeriod: period,
        mode
      });

      setOk('Draw simulation created.');
      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function publish(id) {
    setLoading(true);
    setError('');
    setOk('');

    try {
      await drawsApi.publish(id);

      setOk('Draw published.');
      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Page
      title="Draw control"
      subtitle="Simulate and publish draw results."
    >
      <ErrorBox message={error} />
      <SuccessBox message={ok} />

      <Card className="form-card">

        <div className="section-head">
          <div>
            <h3>Run simulation</h3>

            <p>
              Create a new simulated draw using the backend.
            </p>
          </div>
        </div>

        <div className="form-grid">

          <label>
            Draw period

            <input
              type="date"
              value={period}
              onChange={e => setPeriod(e.target.value)}
            />
          </label>

          <label>
            Mode

            <select
              value={mode}
              onChange={e => setMode(e.target.value)}
            >
              <option value="RANDOM">
                RANDOM
              </option>

              <option value="ALGORITHMIC">
                ALGORITHMIC
              </option>
            </select>
          </label>

          <div
            className="form-actions"
            style={{
              marginTop: '8px'
            }}
          >
            <Button
              loading={loading}
              onClick={simulate}
            >
              Simulate draw
            </Button>
          </div>

        </div>
      </Card>

      <Card>

        <div className="section-head">
          <div>
            <h3>Draw history</h3>
          </div>
        </div>

        {draws.length ? (

          draws.map(d => (
            <div
              className="data-row"
              key={d.id}
            >

              <div className="grow">

                <b>
                  {formatDate(d.drawPeriod)}
                </b>

                <small>
                  {d.participantCount} participants · {d.mode}
                </small>

              </div>

              <Badge tone={statusTone(d.status)}>
                {d.status}
              </Badge>

              <strong>
                {d.drawnNumbers?.join(' · ') || '—'}
              </strong>

              {d.status === 'SIMULATED' && (
                <Button
                  loading={loading}
                  onClick={() => publish(d.id)}
                >
                  Publish
                </Button>
              )}

            </div>
          ))

        ) : (

          <Empty
            title="No draws"
            text="No draw records exist yet."
          />

        )}

      </Card>
    </Page>
  );
}


export function AdminWinners() {
  const [winners, setWinners] = useState([]);
  const [error, setError] = useState('');
  const [ok, setOk] = useState('');
  const [notes, setNotes] = useState('');
  const [selected, setSelected] = useState(null);
  const [loadingAction, setLoadingAction] = useState(null);

  const load = async () => {
    try {
      setError('');

      const data = await winnersApi.all();

      setWinners(data);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  // =========================================================
  // APPROVE / REJECT
  // =========================================================

  async function verify(approved) {
    if (!selected) return;

    setLoadingAction(
      approved ? 'approve' : 'reject'
    );

    setError('');
    setOk('');

    try {
      await winnersApi.verify(selected.id, {
        approved,
        notes
      });

      setOk(
        approved
          ? 'Winner approved.'
          : 'Winner rejected.'
      );

      setSelected(null);
      setNotes('');

      await load();

    } catch (e) {
      setError(e.message);

    } finally {
      setLoadingAction(null);
    }
  }

  // =========================================================
  // MARK PAYOUT AS PAID
  // =========================================================

  async function paid(payoutId) {
    setLoadingAction(`paid-${payoutId}`);

    setError('');
    setOk('');

    try {
      await winnersApi.markPaid(payoutId);

      setOk('Payout marked as paid.');

      await load();

    } catch (e) {
      setError(e.message);

    } finally {
      setLoadingAction(null);
    }
  }

  return (
    <Page
      title="Winners & payouts"
      subtitle="Review verification and payout actions."
    >

      <ErrorBox message={error} />
      <SuccessBox message={ok} />

      <Card>

        {winners.length ? (

          winners.map(w => (

            <div
              className="data-row"
              key={w.id}
              style={{
                alignItems: 'center',
                gap: '14px'
              }}
            >

              {/* WINNER ICON */}

              <div className="winner-badge small">
                ★
              </div>

              {/* WINNER INFO */}

              <div className="grow">

                <b>
                  {w.userName}
                </b>

                <small>
                  {w.tier}
                  {' · '}
                  {formatMoney(w.amount)}
                  {' · '}
                  Draw #{w.drawId}
                </small>

              </div>

              {/* WINNER STATUS */}

              <Badge
                tone={statusTone(w.verificationStatus)}
              >
                {w.verificationStatus}
              </Badge>

              {/* REVIEW */}

              {w.verificationStatus ===
                'PENDING_VERIFICATION' && (

                <Button
                  disabled={loadingAction !== null}
                  onClick={() => setSelected(w)}
                >
                  Review
                </Button>

              )}

              {/* PAYOUT */}

              {w.verificationStatus === 'APPROVED' && (

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px'
                  }}
                >

                  <Badge
                    tone={
                      w.payoutStatus === 'PAID'
                        ? 'success'
                        : 'warning'
                    }
                  >
                    {w.payoutStatus || 'PENDING'}
                  </Badge>

                  {w.payoutStatus === 'PENDING' &&
                    w.payoutId && (

                    <Button
                      loading={
                        loadingAction ===
                        `paid-${w.payoutId}`
                      }
                      disabled={
                        loadingAction !== null
                      }
                      onClick={() =>
                        paid(w.payoutId)
                      }
                    >
                      Mark Paid
                    </Button>

                  )}

                </div>

              )}

            </div>

          ))

        ) : (

          <Empty
            title="No winners"
            text="Winner records will appear here."
          />

        )}

      </Card>

      {/* =====================================================
          REVIEW MODAL
      ===================================================== */}

      <Modal
        open={!!selected}
        title="Review winner"
        onClose={() => {

          if (!loadingAction) {
            setSelected(null);
            setNotes('');
          }

        }}
      >

        <p>
          <b>
            {selected?.userName}
          </b>
          {' · '}
          {selected?.tier}
          {' · '}
          {formatMoney(selected?.amount)}
        </p>

        <label>
          Notes

          <textarea
            value={notes}
            onChange={e =>
              setNotes(e.target.value)
            }
            rows="4"
            placeholder="Verification notes..."
            disabled={loadingAction !== null}
          />

        </label>

        <div
          className="modal-actions"
          style={{
            marginTop: '20px',
            display: 'flex',
            gap: '12px'
          }}
        >

          {/* REJECT */}

          <Button
            variant="danger"
            loading={
              loadingAction === 'reject'
            }
            disabled={
              loadingAction !== null &&
              loadingAction !== 'reject'
            }
            onClick={() => verify(false)}
          >
            Reject
          </Button>

          {/* APPROVE */}

          <Button
            loading={
              loadingAction === 'approve'
            }
            disabled={
              loadingAction !== null &&
              loadingAction !== 'approve'
            }
            onClick={() => verify(true)}
          >
            Approve
          </Button>

        </div>

      </Modal>

    </Page>
  );
}

/* =========================================================
   ADMIN USERS
========================================================= */

export function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(null);

  const load = async () => {
    try {
      const data = await adminApi.users();
      setUsers(data);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  async function toggle(u) {
    setLoading(u.id);
    setError('');

    try {
      await adminApi.setUserActive(
        u.id,
        !u.active
      );

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  return (
    <Page
      title="Users"
      subtitle="Manage account access and status."
    >
      <ErrorBox message={error} />

      <Card>
        {users.length ? (
          users.map(u => (
            <div
              className="data-row user-row"
              key={u.id}
            >
              <div className="avatar small">
                {u.fullName?.[0]?.toUpperCase() || 'U'}
              </div>

              <div className="grow user-info">
                <b>{u.fullName}</b>

                <small>
                  {u.email} · #{u.id}
                </small>
              </div>

              <div className="user-status">
                <Badge>
                  {u.role}
                </Badge>

                <Badge
                  tone={u.active ? 'good' : 'bad'}
                >
                  {u.active ? 'ACTIVE' : 'INACTIVE'}
                </Badge>
              </div>

              <Button
                variant={
                  u.active
                    ? 'danger'
                    : 'secondary'
                }
                loading={loading === u.id}
                onClick={() => toggle(u)}
              >
                {u.active
                  ? 'Deactivate'
                  : 'Activate'}
              </Button>
            </div>
          ))
        ) : (
          <Empty title="No users" />
        )}
      </Card>
    </Page>
  );
}

/* =========================================================
   ADMIN CONTENT
========================================================= */

export function AdminContent() {
  const [tab, setTab] = useState('plans');

  const [plans, setPlans] = useState([]);
  const [charities, setCharities] = useState([]);

  const [error, setError] = useState('');
  const [ok, setOk] = useState('');

  const [loading, setLoading] = useState(null);

  const [editingPlan, setEditingPlan] = useState(null);
  const [editingCharity, setEditingCharity] = useState(null);

  const [plan, setPlan] = useState({
    name: '',
    interval: 'MONTHLY',
    price: '',
    currency: 'INR',
    active: true,
    stripePriceId: ''
  });

  const [charity, setCharity] = useState({
    name: '',
    slug: '',
    description: '',
    logoUrl: '',
    featured: false
  });

  /* =======================================================
     LOAD ADMIN DATA
  ======================================================= */

  const load = async () => {
    setError('');

    try {
      const [planData, charityData] = await Promise.all([
        plansApi.adminList(),
        charitiesApi.adminList()
      ]);

      setPlans(planData);
      setCharities(charityData);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  /* =======================================================
     CREATE PLAN
  ======================================================= */

  async function createPlan(e) {
    e.preventDefault();

    setError('');
    setOk('');
    setLoading('create-plan');

    try {
      await plansApi.create({
        ...plan,
        price: Number(plan.price)
      });

      setOk('Plan created successfully.');

      setPlan({
        name: '',
        interval: 'MONTHLY',
        price: '',
        currency: 'INR',
        active: true,
        stripePriceId: ''
      });

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  /* =======================================================
     CREATE CHARITY
  ======================================================= */

  async function createCharity(e) {
    e.preventDefault();

    setError('');
    setOk('');
    setLoading('create-charity');

    try {
      await charitiesApi.adminCreate(charity);

      setOk('Charity created successfully.');

      setCharity({
        name: '',
        slug: '',
        description: '',
        logoUrl: '',
        featured: false
      });

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  /* =======================================================
     UPDATE PLAN
  ======================================================= */

  async function updatePlan(e) {
    e.preventDefault();

    if (!editingPlan) {
      return;
    }

    setError('');
    setOk('');
    setLoading(`edit-plan-${editingPlan.id}`);

    try {
      await plansApi.update(editingPlan.id, {
        name: editingPlan.name,
        interval: editingPlan.interval,
        price: Number(editingPlan.price),
        currency: editingPlan.currency,
        stripePriceId: editingPlan.stripePriceId
      });

      setOk('Plan updated successfully.');

      setEditingPlan(null);

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  /* =======================================================
     ACTIVATE / DEACTIVATE PLAN
  ======================================================= */

  async function togglePlan(planItem) {
    setError('');
    setOk('');
    setLoading(`plan-status-${planItem.id}`);

    try {
      await plansApi.setActive(
        planItem.id,
        !planItem.active
      );

      setOk(
        planItem.active
          ? 'Plan deactivated successfully.'
          : 'Plan activated successfully.'
      );

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  /* =======================================================
     UPDATE CHARITY
  ======================================================= */

  async function updateCharity(e) {
    e.preventDefault();

    if (!editingCharity) {
      return;
    }

    setError('');
    setOk('');
    setLoading(`edit-charity-${editingCharity.id}`);

    try {
      await charitiesApi.adminUpdate(
        editingCharity.id,
        {
          name: editingCharity.name,
          slug: editingCharity.slug,
          description: editingCharity.description,
          logoUrl: editingCharity.logoUrl,
          featured: editingCharity.featured
        }
      );

      setOk('Charity updated successfully.');

      setEditingCharity(null);

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  /* =======================================================
     ARCHIVE / UNARCHIVE CHARITY
  ======================================================= */

  async function toggleCharity(charityItem) {
    setError('');
    setOk('');
    setLoading(`charity-status-${charityItem.id}`);

    try {
      await charitiesApi.setArchived(
        charityItem.id,
        !charityItem.archived
      );

      setOk(
        charityItem.archived
          ? 'Charity restored successfully.'
          : 'Charity archived successfully.'
      );

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  return (
    <Page
      title="Plans & charities"
      subtitle="Create and manage the catalog content exposed by the platform."
    >
      <ErrorBox message={error} />
      <SuccessBox message={ok} />

      {/* =====================================================
          TABS
      ===================================================== */}

      <div className="tabs">
        <button
          className={tab === 'plans' ? 'active' : ''}
          onClick={() => setTab('plans')}
        >
          Plans
        </button>

        <button
          className={tab === 'charities' ? 'active' : ''}
          onClick={() => setTab('charities')}
        >
          Charities
        </button>
      </div>

      {/* =====================================================
          PLANS
      ===================================================== */}

      {tab === 'plans' ? (
        <div className="two-col">

          {/* CREATE PLAN */}

          <Card>
            <h3>Create plan</h3>

            <form
              onSubmit={createPlan}
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '18px'
              }}
            >
              <label>
                Name

                <input
                  value={plan.name}
                  onChange={e =>
                    setPlan({
                      ...plan,
                      name: e.target.value
                    })
                  }
                  required
                />
              </label>

              <label>
                Interval

                <select
                  value={plan.interval}
                  onChange={e =>
                    setPlan({
                      ...plan,
                      interval: e.target.value
                    })
                  }
                >
                  <option value="MONTHLY">
                    MONTHLY
                  </option>

                  <option value="YEARLY">
                    YEARLY
                  </option>
                </select>
              </label>

              <label>
                Price

                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={plan.price}
                  onChange={e =>
                    setPlan({
                      ...plan,
                      price: e.target.value
                    })
                  }
                  required
                />
              </label>

              <label>
                Currency

                <input
                  value={plan.currency}
                  onChange={e =>
                    setPlan({
                      ...plan,
                      currency: e.target.value.toUpperCase()
                    })
                  }
                  maxLength="3"
                  required
                />
              </label>

              <label>
                Stripe price ID

                <input
                  value={plan.stripePriceId}
                  onChange={e =>
                    setPlan({
                      ...plan,
                      stripePriceId: e.target.value
                    })
                  }
                />
              </label>

              <div
                style={{
                  marginTop: '4px',
                  paddingTop: '4px'
                }}
              >
                <Button
                  loading={loading === 'create-plan'}
                >
                  Create plan
                </Button>
              </div>
            </form>
          </Card>

          {/* PLAN LIST */}

          <Card>
            <div className="section-head">
              <div>
                <h3>All plans</h3>

                <p>
                  Active and inactive plans are shown here.
                </p>
              </div>
            </div>

            {plans.length ? (
              plans.map(p => (
                <div
                  className="data-row"
                  key={p.id}
                >
                  <div className="grow">
                    <b>{p.name}</b>

                    <small>
                      {p.interval} · Plan #{p.id}
                    </small>
                  </div>

                  <strong>
                    {formatMoney(
                      p.price,
                      p.currency
                    )}
                  </strong>

                  <Badge
                    tone={
                      p.active
                        ? 'good'
                        : 'bad'
                    }
                  >
                    {p.active
                      ? 'ACTIVE'
                      : 'INACTIVE'}
                  </Badge>

                  <Button
                    variant="secondary"
                    onClick={() =>
                      setEditingPlan({
                        ...p,
                        price: String(p.price ?? '')
                      })
                    }
                  >
                    Edit
                  </Button>

                  <Button
                    variant={
                      p.active
                        ? 'danger'
                        : 'secondary'
                    }
                    loading={
                      loading ===
                      `plan-status-${p.id}`
                    }
                    onClick={() =>
                      togglePlan(p)
                    }
                  >
                    {p.active
                      ? 'Deactivate'
                      : 'Activate'}
                  </Button>
                </div>
              ))
            ) : (
              <Empty
                title="No plans"
                text="Create your first subscription plan."
              />
            )}
          </Card>
        </div>
      ) : (

        /* =====================================================
           CHARITIES
        ===================================================== */

        <div className="two-col">

          {/* CREATE CHARITY */}

          <Card>
            <h3>Create charity</h3>

            <form
              onSubmit={createCharity}
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '18px'
              }}
            >
              <label>
                Name

                <input
                  value={charity.name}
                  onChange={e =>
                    setCharity({
                      ...charity,
                      name: e.target.value
                    })
                  }
                  required
                />
              </label>

              <label>
                Slug

                <input
                  value={charity.slug}
                  onChange={e =>
                    setCharity({
                      ...charity,
                      slug: e.target.value
                    })
                  }
                  required
                />
              </label>

              <label>
                Description

                <textarea
                  value={charity.description}
                  onChange={e =>
                    setCharity({
                      ...charity,
                      description: e.target.value
                    })
                  }
                  rows="4"
                />
              </label>

              <label>
                Logo URL

                <input
                  value={charity.logoUrl}
                  onChange={e =>
                    setCharity({
                      ...charity,
                      logoUrl: e.target.value
                    })
                  }
                />
              </label>

              <label className="check">
                <input
                  type="checkbox"
                  checked={charity.featured}
                  onChange={e =>
                    setCharity({
                      ...charity,
                      featured: e.target.checked
                    })
                  }
                />

                Featured
              </label>

              <div
                style={{
                  marginTop: '4px',
                  paddingTop: '4px'
                }}
              >
                <Button
                  loading={
                    loading === 'create-charity'
                  }
                >
                  Create charity
                </Button>
              </div>
            </form>
          </Card>

          {/* CHARITY LIST */}

          <Card>
            <div className="section-head">
              <div>
                <h3>All charities</h3>

                <p>
                  Active and archived charities are shown here.
                </p>
              </div>
            </div>

            {charities.length ? (
              charities.map(c => (
                <div
                  className="data-row"
                  key={c.id}
                >
                  <div className="grow">
                    <b>{c.name}</b>

                    <small>
                      {c.slug} · Charity #{c.id}
                    </small>
                  </div>

                  {c.featured && (
                    <Badge>
                      FEATURED
                    </Badge>
                  )}

                  <Badge
                    tone={
                      c.archived
                        ? 'bad'
                        : 'good'
                    }
                  >
                    {c.archived
                      ? 'ARCHIVED'
                      : 'ACTIVE'}
                  </Badge>

                  <Button
                    variant="secondary"
                    onClick={() =>
                      setEditingCharity({
                        ...c
                      })
                    }
                  >
                    Edit
                  </Button>

                  <Button
                    variant={
                      c.archived
                        ? 'secondary'
                        : 'danger'
                    }
                    loading={
                      loading ===
                      `charity-status-${c.id}`
                    }
                    onClick={() =>
                      toggleCharity(c)
                    }
                  >
                    {c.archived
                      ? 'Unarchive'
                      : 'Archive'}
                  </Button>
                </div>
              ))
            ) : (
              <Empty
                title="No charities"
                text="Create your first charity."
              />
            )}
          </Card>
        </div>
      )}

      {/* =====================================================
          EDIT PLAN MODAL
      ===================================================== */}

      <Modal
        open={!!editingPlan}
        title="Edit plan"
        onClose={() => setEditingPlan(null)}
      >
        {editingPlan && (
          <form
            onSubmit={updatePlan}
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '18px'
            }}
          >
            <label>
              Name

              <input
                value={editingPlan.name}
                onChange={e =>
                  setEditingPlan({
                    ...editingPlan,
                    name: e.target.value
                  })
                }
                required
              />
            </label>

            <label>
              Interval

              <select
                value={editingPlan.interval}
                onChange={e =>
                  setEditingPlan({
                    ...editingPlan,
                    interval: e.target.value
                  })
                }
              >
                <option value="MONTHLY">
                  MONTHLY
                </option>

                <option value="YEARLY">
                  YEARLY
                </option>
              </select>
            </label>

            <label>
              Price

              <input
                type="number"
                min="0"
                step="0.01"
                value={editingPlan.price}
                onChange={e =>
                  setEditingPlan({
                    ...editingPlan,
                    price: e.target.value
                  })
                }
                required
              />
            </label>

            <label>
              Currency

              <input
                value={editingPlan.currency}
                onChange={e =>
                  setEditingPlan({
                    ...editingPlan,
                    currency:
                      e.target.value.toUpperCase()
                  })
                }
                maxLength="3"
                required
              />
            </label>

            <label>
              Stripe price ID

              <input
                value={
                  editingPlan.stripePriceId || ''
                }
                onChange={e =>
                  setEditingPlan({
                    ...editingPlan,
                    stripePriceId:
                      e.target.value
                  })
                }
              />
            </label>

            <div
              className="modal-actions"
              style={{
                marginTop: '8px',
                display: 'flex',
                gap: '12px'
              }}
            >
              <Button
                variant="secondary"
                type="button"
                onClick={() =>
                  setEditingPlan(null)
                }
              >
                Cancel
              </Button>

              <Button
                loading={
                  loading ===
                  `edit-plan-${editingPlan.id}`
                }
              >
                Save changes
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {/* =====================================================
          EDIT CHARITY MODAL
      ===================================================== */}

      <Modal
        open={!!editingCharity}
        title="Edit charity"
        onClose={() => setEditingCharity(null)}
      >
        {editingCharity && (
          <form
            onSubmit={updateCharity}
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '18px'
            }}
          >
            <label>
              Name

              <input
                value={editingCharity.name}
                onChange={e =>
                  setEditingCharity({
                    ...editingCharity,
                    name: e.target.value
                  })
                }
                required
              />
            </label>

            <label>
              Slug

              <input
                value={editingCharity.slug}
                onChange={e =>
                  setEditingCharity({
                    ...editingCharity,
                    slug: e.target.value
                  })
                }
                required
              />
            </label>

            <label>
              Description

              <textarea
                value={
                  editingCharity.description || ''
                }
                onChange={e =>
                  setEditingCharity({
                    ...editingCharity,
                    description:
                      e.target.value
                  })
                }
                rows="4"
              />
            </label>

            <label>
              Logo URL

              <input
                value={
                  editingCharity.logoUrl || ''
                }
                onChange={e =>
                  setEditingCharity({
                    ...editingCharity,
                    logoUrl: e.target.value
                  })
                }
              />
            </label>

            <label className="check">
              <input
                type="checkbox"
                checked={
                  !!editingCharity.featured
                }
                onChange={e =>
                  setEditingCharity({
                    ...editingCharity,
                    featured:
                      e.target.checked
                  })
                }
              />

              Featured
            </label>

            <div
              className="modal-actions"
              style={{
                marginTop: '8px',
                display: 'flex',
                gap: '12px'
              }}
            >
              <Button
                variant="secondary"
                type="button"
                onClick={() =>
                  setEditingCharity(null)
                }
              >
                Cancel
              </Button>

              <Button
                loading={
                  loading ===
                  `edit-charity-${editingCharity.id}`
                }
              >
                Save changes
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </Page>
  );
}