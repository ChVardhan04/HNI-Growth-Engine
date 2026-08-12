import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';

const ACTIVITIES = [
  'EMAIL_OPEN', 'EMAIL_CLICK', 'PDF_DOWNLOAD', 'WEBSITE_VISIT', 'LINKEDIN_CLICK',
  'WHATSAPP_REPLY', 'WEBINAR', 'CALL', 'MEETING', 'DEMO', 'ADVISOR_BOOKING',
  'CONNECTION_SENT', 'CONNECTION_ACCEPTED', 'VIEWED_PROFILE', 'VIEWED_MESSAGE',
  'REPLIED', 'POSITIVE_REPLY', 'MEETING_REQUESTED', 'PROFILE_CLICK',
  'REPEATED_REPLY', 'MULTIPLE_ENGAGEMENT',
];
const CHANNELS = ['EMAIL', 'LINKEDIN', 'WHATSAPP', 'SMS'];
const STAGES = ['CONNECTION_REQUEST', 'FIRST_MESSAGE', 'FOLLOW_UP_1', 'FOLLOW_UP_2', 'FOLLOW_UP_3'];
const LENGTHS = ['SHORT', 'LONG'];

export default function LeadDetail() {
  const { id } = useParams();
  const [lead, setLead] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [messages, setMessages] = useState([]);
  const [activity, setActivity] = useState('EMAIL_OPEN');
  const [channel, setChannel] = useState('EMAIL');
  const [stage, setStage] = useState('FIRST_MESSAGE');
  const [length, setLength] = useState('LONG');
  const [busy, setBusy] = useState(false);

  const loadAll = () => {
    api.get('/leads/' + id).then((r) => setLead(r.data.data));
    api.get('/engagement/' + id + '/timeline').then((r) => setTimeline(r.data.data));
    api.get('/messages/lead/' + id).then((r) => setMessages(r.data.data));
  };

  useEffect(() => { loadAll(); /* eslint-disable-next-line */ }, [id]);

  const track = async () => {
    setBusy(true);
    try {
      const { data } = await api.post(`/engagement/${id}/track`, null, { params: { type: activity } });
      setLead(data.data);
      loadAll();
    } finally { setBusy(false); }
  };

  const generate = async () => {
    setBusy(true);
    try {
      await api.post('/messages/generate', { leadId: Number(id), channel, stage, length });
      loadAll();
    } finally { setBusy(false); }
  };

  const regenerate = async (messageId) => {
    setBusy(true);
    try {
      await api.post(`/messages/${messageId}/regenerate`);
      loadAll();
    } finally { setBusy(false); }
  };

  const rescore = async () => {
    setBusy(true);
    try {
      const { data } = await api.post('/leads/' + id + '/rescore');
      setLead(data.data);
    } finally { setBusy(false); }
  };

  if (!lead) return <div className="skeleton" style={{ height: 300 }} />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <Link to="/leads" style={{ color: 'var(--muted)', fontSize: '0.85rem' }}>← Back to leads</Link>
          <h4 style={{ fontWeight: 700 }} className="mb-0">{lead.name}</h4>
        </div>
        <button className="btn btn-outline-primary" onClick={rescore} disabled={busy}>Re-score with AI</button>
      </div>

      <div className="row g-3">
        <div className="col-md-4">
          <div className="panel mb-3">
            <h6>Profile</h6>
            {lead.profileImageUrl && (
              <img src={lead.profileImageUrl} alt={lead.name} width={64} height={64}
                   style={{ borderRadius: '50%', objectFit: 'cover', marginBottom: 10 }} />
            )}
            <p className="mb-1"><b>Company:</b> {lead.company || '-'}</p>
            <p className="mb-1"><b>Designation:</b> {lead.designation || '-'}</p>
            <p className="mb-1"><b>Industry:</b> {lead.industry || '-'}</p>
            <p className="mb-1"><b>Location:</b> {lead.location || '-'}</p>
            <p className="mb-1"><b>Experience:</b> {lead.yearsExperience ?? '-'} yrs</p>
            <p className="mb-1"><b>Company Size:</b> {lead.companySize || '-'}</p>
            <p className="mb-1"><b>Connection Degree:</b> {lead.connectionDegree || '-'}</p>
            <p className="mb-1"><b>Skills:</b> {lead.skills || '-'}</p>
            <p className="mb-1"><b>Education:</b> {lead.education || '-'}</p>
            <p className="mb-1"><b>Email:</b> {lead.email || '-'}</p>
            <p className="mb-1"><b>Source:</b> {lead.source}</p>
            <p className="mb-1"><b>Status:</b> {lead.status}</p>
            {lead.campaignId && (
              <p className="mb-1"><b>Campaign:</b> <Link to={`/campaigns/${lead.campaignId}`}>#{lead.campaignId}</Link></p>
            )}
            {lead.linkedinUrl && (
              <p className="mb-1"><a href={lead.linkedinUrl} target="_blank" rel="noreferrer">View LinkedIn profile →</a></p>
            )}
            <p className="mb-0"><b>Assigned RM:</b> {lead.assignedRmName || '-'}</p>
          </div>
        </div>

        <div className="col-md-8">
          <div className="panel mb-3">
            <h6>AI Insights <span className="text-muted" style={{ fontSize: '0.8rem' }}>(explainable)</span></h6>
            <div className="row text-center my-3">
              <div className="col">
                <div style={{ fontSize: '1.8rem', fontWeight: 700 }}>{lead.icpScore}</div>
                <div className="text-muted">ICP Score</div>
              </div>
              <div className="col">
                <div><span className={'badge-tier tier-' + lead.tier}>{lead.tier}</span></div>
                <div className="text-muted mt-2">Tier</div>
              </div>
              <div className="col">
                <div style={{ fontSize: '1.8rem', fontWeight: 700 }}>{lead.icpConfidence}%</div>
                <div className="text-muted">Confidence</div>
              </div>
              <div className="col">
                <div><span className={'band band-' + lead.intentBand}>{lead.intentBand} · {lead.intentScore}</span></div>
                <div className="text-muted mt-2">Intent</div>
              </div>
            </div>
            <div className="alert alert-light border">
              <b>Why:</b> {lead.icpReason}
            </div>
            <div className="alert alert-primary">
              <b>Next Best Action:</b> {lead.nextBestAction} - {lead.recommendationReason}
            </div>
          </div>

          <div className="panel mb-3">
            <h6>Track Engagement</h6>
            <div className="d-flex gap-2">
              <select className="form-select" value={activity} onChange={(e) => setActivity(e.target.value)}>
                {ACTIVITIES.map((a) => <option key={a}>{a}</option>)}
              </select>
              <button className="btn btn-brand" onClick={track} disabled={busy}>Record</button>
            </div>
          </div>

          <div className="panel">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0">Outreach Messages</h6>
              <div className="d-flex gap-2">
                <select className="form-select form-select-sm" value={channel} onChange={(e) => setChannel(e.target.value)}>
                  {CHANNELS.map((c) => <option key={c}>{c}</option>)}
                </select>
                <select className="form-select form-select-sm" value={stage} onChange={(e) => setStage(e.target.value)}>
                  {STAGES.map((s) => <option key={s}>{s}</option>)}
                </select>
                <select className="form-select form-select-sm" value={length} onChange={(e) => setLength(e.target.value)}>
                  {LENGTHS.map((l) => <option key={l}>{l}</option>)}
                </select>
                <button className="btn btn-sm btn-brand" onClick={generate} disabled={busy}>Generate</button>
              </div>
            </div>
            {messages.length === 0 && <p className="text-muted mb-0">No messages yet.</p>}
            {messages.map((m) => (
              <div key={m.id} className="border rounded p-2 mb-2">
                <div className="d-flex justify-content-between">
                  <b>{m.channel} · {m.stage}</b>
                  <span className="badge bg-secondary">{m.status}</span>
                </div>
                {m.subject && <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{m.subject}</div>}
                <div style={{ fontSize: '0.85rem', whiteSpace: 'pre-wrap' }}>{m.body}</div>
                {m.lastError && (
                  <div className="alert alert-warning py-1 px-2 mt-2 mb-0" style={{ fontSize: '0.8rem' }}>
                    ⚠️ {m.lastError}
                  </div>
                )}
                {m.status !== 'SENT' && (
                  <button className="btn btn-sm btn-outline-secondary mt-2" onClick={() => regenerate(m.id)} disabled={busy}>
                    Regenerate
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="panel mt-3">
        <h6>Activity Timeline</h6>
        {timeline.length === 0 && <p className="text-muted mb-0">No activity recorded.</p>}
        <table className="table-clean">
          <tbody>
            {timeline.map((t) => (
              <tr key={t.id}>
                <td>{t.type}</td>
                <td>+{t.weightApplied}</td>
                <td>{new Date(t.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
