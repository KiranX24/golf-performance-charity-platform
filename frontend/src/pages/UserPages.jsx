import React, { useEffect, useState } from 'react';

import {
  charitiesApi,
  drawsApi,
  plansApi,
  scoresApi,
  subscriptionsApi,
  winnersApi,
  userApi
} from '../api/client';

import {
  Badge,
  Button,
  Card,
  Empty,
  ErrorBox,
  Modal,
  Spinner,
  StatCard,
  SuccessBox,
  formatDate,
  formatMoney,
  statusTone,
  Page
} from '../components/UI';


export function Overview({ user }) {

  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    userApi.dashboard()
      .then(setData)
      .catch(e => setError(e.message));
  }, []);

  if (error) {
    return (
      <Page title="Overview">
        <ErrorBox message={error} />
      </Page>
    );
  }

  if (!data) {
    return (
      <Page title="Overview">
        <Spinner />
      </Page>
    );
  }

  const scores = data.scores || [];
  const winners = data.winners || [];

  return (
    <Page
      title={`Good to see you, ${user.fullName.split(' ')[0]}.`}
      subtitle="Your A Golf Performance and Charity Draw Platform activity at a glance."
    >
      <div className="hero-card">
        <div>
          <span className="eyebrow light">YOUR HERO HUB</span>
          <h1>Play with purpose.</h1>
          <p>
            Keep your scores current, choose your cause, and stay ready for the next draw.
          </p>
        </div>

        <div className="hero-stat">
          <strong>{scores.length}</strong>
          <span>scores tracked</span>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard
          label="Subscription"
          value={data.subscription?.planName || 'None'}
          hint={data.subscription?.status || 'Choose a plan'}
        />

        <StatCard
          label="Charity contribution"
          value={data.charity ? `${data.charity.contributionPct}%` : '—'}
          hint={data.charity?.charityName || 'Choose a charity'}
        />

        <StatCard
          label="Winning records"
          value={winners.length}
          hint="Your verified draw history"
        />

        <StatCard
          label="Latest score"
          value={scores[0]?.scoreValue ?? '—'}
          hint={
            scores[0]
              ? formatDate(scores[0].scoreDate)
              : 'Add your first score'
          }
        />
      </div>

      <div className="two-col">

        <Card>
          <div className="section-head">
            <div>
              <h3>Recent scores</h3>
              <p>Latest performance records.</p>
            </div>
          </div>

          {scores.length ? (
            scores.slice(0, 5).map(s => (
              <div className="list-row" key={s.id}>
                <div className="score-ball">{s.scoreValue}</div>

                <div>
                  <b>Stableford score</b>
                  <small>{formatDate(s.scoreDate)}</small>
                </div>
              </div>
            ))
          ) : (
            <Empty
              title="No scores yet"
              text="Add your latest Stableford round to get started."
            />
          )}
        </Card>


        <Card>
          <div className="section-head">
            <div>
              <h3>Winnings</h3>
              <p>Your winner records.</p>
            </div>
          </div>

          {winners.length ? (
            winners.map(w => (
              <div className="list-row" key={w.id}>
                <div className="icon-box">★</div>

                <div>
                  <b>{w.tier} tier</b>
                  <small>{formatMoney(w.amount)}</small>
                </div>

                <Badge tone={statusTone(w.verificationStatus)}>
                  {w.verificationStatus}
                </Badge>
              </div>
            ))
          ) : (
            <Empty
              title="No winnings yet"
              text="Your winning records will appear here."
            />
          )}
        </Card>

      </div>
    </Page>
  );
}


export function Plans() {
  const [plans, setPlans] = useState([]);
  const [subs, setSubs] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(null);
  const [ok, setOk] = useState('');

  const load = async () => {
    try {
      setError('');

      const [p, s] = await Promise.all([
        plansApi.list(),
        subscriptionsApi.mine()
      ]);

      setPlans(p);
      setSubs(s);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  // =========================
  // STRIPE CHECKOUT
  // =========================
  async function checkout(id) {
    setLoading(id);
    setError('');
    setOk('');

    try {
      const response = await subscriptionsApi.checkout(id);

      if (!response?.checkoutUrl) {
        throw new Error(
          'Stripe Checkout URL was not returned by the server.'
        );
      }

      // Redirect user to Stripe Checkout
      window.location.href = response.checkoutUrl;

    } catch (e) {
      setError(e.message);
      setLoading(null);
    }
  }

  // =========================
  // CANCEL SUBSCRIPTION
  // =========================
  async function cancel() {
    setLoading('cancel');
    setError('');
    setOk('');

    try {
      await subscriptionsApi.cancel();

      setOk('Subscription cancelled successfully.');

      await load();

    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(null);
    }
  }

  const active = subs.find(
    x => x.status === 'ACTIVE'
  );

  return (
    <Page
      title="Plans"
      subtitle="Choose the membership that fits your journey."
    >

      <ErrorBox message={error} />

      <SuccessBox message={ok} />

      {/* =========================
          CURRENT SUBSCRIPTION
      ========================= */}
      {active && (
        <Card className="active-sub">

          <div>
            <span className="eyebrow">
              CURRENT PLAN
            </span>

            <h3>
              {active.planName}
            </h3>

            <p>
              {active.status} · renews{' '}
              {active.renewalDate
                ? formatDate(active.renewalDate)
                : 'according to your Stripe billing cycle'}
            </p>
          </div>

          <Button
            variant="danger"
            loading={loading === 'cancel'}
            onClick={cancel}
          >
            Cancel subscription
          </Button>

        </Card>
      )}

      {/* =========================
          PLANS
      ========================= */}
      <div className="plan-grid">

        {plans.map(p => (

          <Card
            className="plan-card"
            key={p.id}
          >

            <Badge>
              {p.interval}
            </Badge>

            <h3>
              {p.name}
            </h3>

            <div className="price">
              {formatMoney(
                p.price,
                p.currency
              )}
            </div>

            <span className="muted">
              per{' '}
              {p.interval
                .toLowerCase()
                .replace('ly', '')}
            </span>

            <Button
              loading={loading === p.id}
              disabled={active != null}
              onClick={() => checkout(p.id)}
            >
              {active ? 'Current subscription' : 'Activate plan'}
            </Button>

          </Card>

        ))}

      </div>

    </Page>
  );
}

export function Scores() {

  const [scores, setScores] = useState([]);
  const [value, setValue] = useState(20);
  const [date, setDate] = useState('');
  const [edit, setEdit] = useState(null);
  const [error, setError] = useState('');
  const [ok, setOk] = useState('');
  const [loading, setLoading] = useState(false);

  const load = async () => {
    try {
      const data = await scoresApi.list();
      setScores(data);
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  async function save(e) {
    e.preventDefault();

    setLoading(true);
    setError('');

    try {
      const body = {
        scoreValue: Number(value),
        scoreDate: date
      };

      if (edit) {
        await scoresApi.update(edit.id, body);
      } else {
        await scoresApi.create(body);
      }

      setOk(edit ? 'Score updated.' : 'Score added.');
      setEdit(null);
      setDate('');
      setValue(20);

      await load();

    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  function startEdit(s) {
    setEdit(s);
    setValue(s.scoreValue);
    setDate(s.scoreDate);
  }

  async function remove(id) {

    if (!confirm('Delete this score?')) {
      return;
    }

    try {
      await scoresApi.remove(id);
      setOk('Score deleted.');
      await load();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <Page
      title="Scores"
      subtitle="Keep your latest Stableford rounds accurate and current."
    >
      <ErrorBox message={error} />
      <SuccessBox message={ok} />

      <Card className="form-card">
        <form className="form-grid" onSubmit={save}>

          <label>
            Stableford score
            <input
              type="number"
              min="1"
              max="45"
              value={value}
              onChange={e => setValue(e.target.value)}
              required
            />
          </label>

          <label>
            Score date
            <input
              type="date"
              value={date}
              onChange={e => setDate(e.target.value)}
              required
            />
          </label>

          <div className="form-actions">
            <Button loading={loading}>
              {edit ? 'Update score' : 'Add score'}
            </Button>

            {edit && (
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setEdit(null);
                  setDate('');
                  setValue(20);
                }}
              >
                Cancel
              </Button>
            )}
          </div>

        </form>
      </Card>

      <Card>
        <div className="section-head">
          <div>
            <h3>Your scores</h3>
            <p>{scores.length} record(s)</p>
          </div>
        </div>

        {scores.length ? (
          <div className="data-list">

            {scores.map(s => (
              <div className="data-row" key={s.id}>

                <div className="score-ball">
                  {s.scoreValue}
                </div>

                <div className="grow">
                  <b>Stableford round</b>
                  <small>{formatDate(s.scoreDate)}</small>
                </div>

                <Button
                  variant="ghost"
                  onClick={() => startEdit(s)}
                >
                  Edit
                </Button>

                <Button
                  variant="danger"
                  onClick={() => remove(s.id)}
                >
                  Delete
                </Button>

              </div>
            ))}

          </div>
        ) : (
          <Empty
            title="No scores"
            text="Add your first round above."
          />
        )}
      </Card>
    </Page>
  );
}


export function Charity() {
  const [items, setItems] = useState([]);
  const [selection, setSelection] = useState(null);

  const [q, setQ] = useState('');
  const [pct, setPct] = useState(10);
  const [amount, setAmount] = useState('');

  const [donateId, setDonateId] = useState(null);

  const [error, setError] = useState('');
  const [ok, setOk] = useState('');

  // IMPORTANT:
  // Store the ID of the charity that is currently being selected.
  // This prevents every charity card from showing "Processing...".
  const [selectLoadingId, setSelectLoadingId] = useState(null);

  const [donateLoading, setDonateLoading] = useState(false);

  async function load() {
    try {
      setError('');

      const [c, s] = await Promise.all([
        charitiesApi.list(q),
        charitiesApi.selection().catch(() => null)
      ]);

      setItems(c);
      setSelection(s);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function select(id) {
    setSelectLoadingId(id);
    setError('');
    setOk('');

    try {
      await charitiesApi.select({
        charityId: id,
        contributionPct: Number(pct)
      });

      setOk('Charity selection updated.');

      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setSelectLoadingId(null);
    }
  }

  async function donate(e) {
    e.preventDefault();

    if (!donateId) {
      return;
    }

    setDonateLoading(true);
    setError('');
    setOk('');

    try {
      await charitiesApi.donate({
        charityId: donateId,
        amount: Number(amount)
      });

      setOk('Donation recorded successfully.');

      setDonateId(null);
      setAmount('');
    } catch (e) {
      setError(e.message);
    } finally {
      setDonateLoading(false);
    }
  }

  return (
    <Page
      title="Charity"
      subtitle="Turn your participation into measurable impact."
    >
      <ErrorBox message={error} />

      <SuccessBox message={ok} />

      {selection && (
        <Card className="selection-card">
          <div>
            <span className="eyebrow">CURRENT SELECTION</span>

            <h3>
              {selection.charityName}
            </h3>

            <p>
              {selection.contributionPct}% contribution · effective{' '}
              {formatDate(selection.effectiveFrom)}
            </p>
          </div>
        </Card>
      )}

      <div className="toolbar">
        <input
          value={q}
          onChange={e => setQ(e.target.value)}
          placeholder="Search charities..."
        />

        <select
          value={pct}
          onChange={e => setPct(e.target.value)}
        >
          <option value="10">
            10% contribution
          </option>

          <option value="25">
            25% contribution
          </option>

          <option value="50">
            50% contribution
          </option>

          <option value="100">
            100% contribution
          </option>
        </select>

        <Button onClick={load}>
          Search
        </Button>
      </div>

      <div className="charity-grid">
        {items.map(c => (
          <Card
            className="charity-card"
            key={c.id}
          >
            {/* TOP ROW */}
            <div
              className="charity-card-top"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                minHeight: '60px',
                marginBottom: '22px'
              }}
            >
              {c.logoUrl ? (
                <img
                  src={c.logoUrl}
                  alt={c.name}
                  style={{
                    width: '60px',
                    height: '60px',
                    objectFit: 'cover',
                    borderRadius: '14px'
                  }}
                />
              ) : (
                <div className="charity-icon">
                  ♥
                </div>
              )}

              {c.featured && (
                <Badge>
                  FEATURED
                </Badge>
              )}
            </div>

            {/* CONTENT */}
            <div
              className="charity-meta"
              style={{
                flex: 1
              }}
            >
              <h3>
                {c.name}
              </h3>

              <p>
                {c.description ||
                  'Supporting a meaningful cause through A Golf Performance and Charity Draw Platform.'}
              </p>
            </div>

            {/* ACTIONS */}
            <div
              className="card-actions"
              style={{
                marginTop: '24px'
              }}
            >
              <Button
                loading={selectLoadingId === c.id}
                disabled={
                  selectLoadingId !== null &&
                  selectLoadingId !== c.id
                }
                onClick={() => select(c.id)}
              >
                Support {pct}%
              </Button>

              <Button
                variant="ghost"
                onClick={() => setDonateId(c.id)}
              >
                Donate
              </Button>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        open={!!donateId}
        title="Make a donation"
        onClose={() => {
          if (!donateLoading) {
            setDonateId(null);
          }
        }}
      >
        <form
          onSubmit={donate}
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '20px'
          }}
        >
          <label
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '8px'
            }}
          >
            Amount

            <input
              type="number"
              min="1"
              step="0.01"
              value={amount}
              onChange={e => setAmount(e.target.value)}
              placeholder="100"
              required
              style={{
                width: '100%',
                boxSizing: 'border-box'
              }}
            />
          </label>

          <div
            style={{
              marginTop: '4px'
            }}
          >
            <Button
              loading={donateLoading}
            >
              Record donation
            </Button>
          </div>
        </form>
      </Modal>
    </Page>
  );
}


export function Draws() {

  const [draws, setDraws] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {

    async function loadDraws() {
      try {
        const data = await drawsApi.list();
        setDraws(data);
      } catch (e) {
        setError(e.message);
      }
    }

    loadDraws();

  }, []);

  return (
    <Page
      title="Draws"
      subtitle="Follow published draw results and upcoming activity."
    >
      <ErrorBox message={error} />

      {draws.length ? (
        <div className="draw-list">

          {draws.map(d => (
            <Card className="draw-card" key={d.id}>

              <div className="draw-date">
                <span>{formatDate(d.drawPeriod)}</span>

                <Badge tone={statusTone(d.status)}>
                  {d.status}
                </Badge>
              </div>

              <div>
                <h3>
                  {d.drawnNumbers?.length
                    ? d.drawnNumbers.join(' · ')
                    : 'Results not published'}
                </h3>

                <p>
                  {d.participantCount} participant(s) · {d.mode}
                </p>
              </div>

            </Card>
          ))}

        </div>
      ) : (
        <Empty
          title="No draws yet"
          text="Published draw information will appear here."
        />
      )}

    </Page>
  );
}


export function Winners() {
  const [winners, setWinners] = useState([]);
  const [error, setError] = useState('');
  const [ok, setOk] = useState('');
  const [files, setFiles] = useState({});
  const [uploadingId, setUploadingId] = useState(null);

  async function loadWinners() {
    try {
      setError('');

      const data = await winnersApi.mine();

      setWinners(data);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    loadWinners();
  }, []);

  async function upload(id) {
    const selectedFile = files[id];

    if (!selectedFile) {
      setError('Please select a proof image first.');
      setOk('');
      return;
    }

    if (!selectedFile.type.startsWith('image/')) {
      setError('Only image proof files are allowed.');
      setOk('');
      return;
    }

    if (selectedFile.size > 5_000_000) {
      setError('Proof image must be 5 MB or smaller.');
      setOk('');
      return;
    }

    setUploadingId(id);
    setError('');
    setOk('');

    try {
      const formData = new FormData();

      formData.append('file', selectedFile);

      await winnersApi.proof(id, formData);

      setOk('Proof uploaded successfully. Your winner is now ready for admin review.');

      setFiles(prev => {
        const updated = { ...prev };
        delete updated[id];
        return updated;
      });

      await loadWinners();

    } catch (e) {
      setError(e.message);
    } finally {
      setUploadingId(null);
    }
  }

  return (
    <Page
      title="Winners"
      subtitle="Your winning records and verification status."
    >

      <ErrorBox message={error} />
      <SuccessBox message={ok} />

      {winners.length ? (

        winners.map(w => (

          <Card className="winner-card" key={w.id}>

            <div className="winner-badge">
              ★
            </div>

            <div className="grow">

              <span className="eyebrow">
                {w.tier} TIER
              </span>

              <h3>
                {formatMoney(w.amount)}
              </h3>

              <p>
                Draw #{w.drawId} · User #{w.userId}
              </p>

            </div>

            <Badge tone={statusTone(w.verificationStatus)}>
              {w.verificationStatus}
            </Badge>

            {w.verificationStatus === 'PENDING_VERIFICATION' && (

              <div
                className="proof"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '10px',
                  marginTop: '15px'
                }}
              >

                <label>
                  Upload proof image
                </label>

                <input
                  type="file"
                  accept="image/*"
                  onChange={e => {
                    const selected = e.target.files?.[0] || null;

                    setFiles(prev => ({
                      ...prev,
                      [w.id]: selected
                    }));

                    setError('');
                    setOk('');
                  }}
                />

                {files[w.id] && (
                  <small>
                    Selected: {files[w.id].name}
                  </small>
                )}

                <Button
                  loading={uploadingId === w.id}
                  disabled={
                    uploadingId !== null &&
                    uploadingId !== w.id
                  }
                  onClick={() => upload(w.id)}
                >
                  Upload proof
                </Button>

                <small>
                  Image only · Maximum 5 MB
                </small>

              </div>

            )}

          </Card>

        ))

      ) : (

        <Empty
          title="No winnings yet"
          text="Winning records will appear here when available."
        />

      )}

    </Page>
  );
}