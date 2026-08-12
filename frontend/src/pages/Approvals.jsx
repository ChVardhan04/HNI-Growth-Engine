import { useEffect, useState } from 'react';
import api from '../api/axios';

export default function Approvals() {
  const [messages, setMessages] = useState([]);
  const [approved, setApproved] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState({}); // id -> {subject, body}
  const [bulkSending, setBulkSending] = useState(false);
  const [bulkResult, setBulkResult] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([
      api.get('/messages/pending'),
      api.get('/messages/approved'),
    ]).then(([p, a]) => {
      setMessages(p.data.data);
      setApproved(a.data.data);
    }).finally(() => setLoading(false));
  };
  useEffect(() => { load(); }, []);

  const startEdit = (m) => setEditing({ ...editing, [m.id]: { subject: m.subject || '', body: m.body || '' } });
  const changeEdit = (id, key, v) => setEditing({ ...editing, [id]: { ...editing[id], [key]: v } });

  const saveEdit = async (id) => {
    await api.put('/messages/' + id, editing[id]);
    const e = { ...editing }; delete e[id]; setEditing(e);
    load();
  };
  const approve = async (id) => { await api.post('/messages/' + id + '/approve'); load(); };
  const reject = async (id) => {
    const comments = prompt('Rejection comments (optional):') || '';
    await api.post('/messages/' + id + '/reject', { comments });
    load();
  };
  const send = async (id) => {
    try {
      await api.post('/messages/' + id + '/send');
      load();
    } catch (e) {
      alert(e.response?.data?.message || 'Send failed.');
      load(); // refresh so the message's lastError / retry count shows up
    }
  };
  const regenerate = async (id) => { await api.post('/messages/' + id + '/regenerate'); load(); };

  const sendAllApproved = async () => {
    setBulkSending(true); setBulkResult(null);
    try {
      const { data } = await api.post('/messages/bulk-send', { messageIds: approved.map((m) => m.id) });
      setBulkResult(data.data);
      load();
    } catch (e) {
      setBulkResult({ error: e.response?.data?.message || 'Bulk send failed.' });
    } finally {
      setBulkSending(false);
    }
  };

  const messageCard = (m, showSend) => (
    <div key={m.id} className="panel mb-3">
      <div className="d-flex justify-content-between mb-2">
        <div><b>{m.channel}</b> · <span className="text-muted">{m.stage}</span> · Lead #{m.leadId} {m.aiGenerated && <span className="badge bg-info ms-2">AI</span>}</div>
        <span className={'badge ' + (m.status === 'APPROVED' ? 'bg-success' : 'bg-warning text-dark')}>{m.status}</span>
      </div>

      {editing[m.id] ? (
        <>
          {m.channel === 'EMAIL' && (
            <input className="form-control mb-2" value={editing[m.id].subject}
                   onChange={(e) => changeEdit(m.id, 'subject', e.target.value)} placeholder="Subject" />
          )}
          <textarea className="form-control mb-2" rows={4} value={editing[m.id].body}
                    onChange={(e) => changeEdit(m.id, 'body', e.target.value)} />
          <button className="btn btn-sm btn-brand me-2" onClick={() => saveEdit(m.id)}>Save</button>
          <button className="btn btn-sm btn-outline-secondary" onClick={() => { const e = { ...editing }; delete e[m.id]; setEditing(e); }}>Cancel</button>
        </>
      ) : (
        <>
          {m.subject && <div style={{ fontWeight: 600 }}>{m.subject}</div>}
          <div style={{ whiteSpace: 'pre-wrap', fontSize: '0.9rem' }} className="mb-2">{m.body}</div>
          {m.lastError && (
            <div className="alert alert-warning py-1 px-2 mb-2" style={{ fontSize: '0.85rem' }}>
              ⚠️ {m.lastError}
            </div>
          )}
          <button className="btn btn-sm btn-outline-primary me-2" onClick={() => startEdit(m)}>Edit</button>
          <button className="btn btn-sm btn-outline-secondary me-2" onClick={() => regenerate(m.id)}>Regenerate</button>
          {showSend ? (
            <>
              <button className="btn btn-sm btn-outline-danger me-2" onClick={() => reject(m.id)}>Reject</button>
              <button className="btn btn-sm btn-brand" onClick={() => send(m.id)}>Send</button>
            </>
          ) : (
            <>
              <button className="btn btn-sm btn-success me-2" onClick={() => approve(m.id)}>Approve</button>
              <button className="btn btn-sm btn-outline-danger" onClick={() => reject(m.id)}>Reject</button>
            </>
          )}
        </>
      )}
    </div>
  );

  if (loading) return <div className="skeleton" style={{ height: 200 }} />;

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">Approval Queue</h4>

      {messages.length === 0 ? (
        <div className="panel text-muted mb-4">Nothing awaiting approval. 🎉</div>
      ) : (
        <>
          <h6 className="text-muted mb-2">Pending Approval ({messages.length})</h6>
          {messages.map((m) => messageCard(m, false))}
        </>
      )}

      {approved.length > 0 && (
        <div className="mt-4">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <h6 className="text-muted mb-0">Approved - Ready to Send ({approved.length})</h6>
            <button className="btn btn-sm btn-primary" disabled={bulkSending} onClick={sendAllApproved}>
              {bulkSending ? `Sending ${approved.length}…` : `Send All ${approved.length} Approved`}
            </button>
          </div>
          {bulkResult && !bulkResult.error && (
            <div className="alert alert-info">
              Sent {bulkResult.succeeded}/{bulkResult.total} messages
              {bulkResult.failed > 0 && ` (${bulkResult.failed} failed - check audit log)`}.
            </div>
          )}
          {bulkResult?.error && <div className="alert alert-danger">{bulkResult.error}</div>}
          {approved.map((m) => messageCard(m, true))}
        </div>
      )}
    </div>
  );
}
