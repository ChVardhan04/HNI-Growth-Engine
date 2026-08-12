import { useEffect, useState } from 'react';
import api from '../api/axios';

const KEYS = [
  { key: 'LINKEDIN_BASE_URL', label: 'LinkedIn/Unipile Base URL', secret: false },
  { key: 'LINKEDIN_API_KEY', label: 'LinkedIn/Unipile API Key', secret: true },
  { key: 'LINKEDIN_ACCOUNT_ID', label: 'LinkedIn Account ID', secret: false },
  { key: 'LINKEDIN_WORKFLOW_ID', label: 'LinkedIn Workflow ID', secret: false },
  { key: 'OPENAI_API_KEY', label: 'OpenAI API Key', secret: true },
  { key: 'CLAUDE_API_KEY', label: 'Claude API Key', secret: true },
  { key: 'GEMINI_API_KEY', label: 'Gemini API Key', secret: true },
  { key: 'SMTP_HOST', label: 'SMTP Host', secret: false },
  { key: 'SMTP_USERNAME', label: 'SMTP Username', secret: false },
  { key: 'SMTP_PASSWORD', label: 'SMTP Password', secret: true },
  { key: 'WEBHOOK_URL', label: 'Webhook URL', secret: false },
];

const INSTRUCTIONS_KEY = 'MESSAGE_GENERATION_INSTRUCTIONS';
const INSTRUCTIONS_DEFAULT_HINT =
  'Add tone guidance and industry-specific talking points here - e.g. "For BFSI prospects, reference ' +
  'regulatory trust and long-term wealth stewardship. For Technology prospects, reference growth stage ' +
  'and equity events." This is prepended to every AI message draft, for every channel and stage.';

export default function Settings() {
  const [saved, setSaved] = useState({});
  const [form, setForm] = useState({});
  const [status, setStatus] = useState('');

  useEffect(() => {
    api.get('/settings').then((r) => {
      const map = {};
      r.data.data.forEach((s) => { map[s.key] = s.value; });
      setSaved(map);
    }).catch(() => {});
  }, []);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async (item) => {
    setStatus('');
    try {
      await api.put('/settings', { key: item.key, value: form[item.key] ?? '', secret: item.secret, description: item.label });
      setStatus(item.key + ' saved.');
    } catch (e) {
      setStatus(e.response?.data?.message || 'Failed to save setting.');
    }
  };

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">Settings - API Keys & Integrations</h4>
      <p className="text-muted">
        Values here are never hardcoded in code. Existing values are masked; enter a new value and save to rotate a key.
      </p>
      {status && <div className="alert alert-info py-2">{status}</div>}

      <div className="panel mb-4">
        <h6>AI Message Generation Instructions</h6>
        <p className="text-muted" style={{ fontSize: 13 }}>{INSTRUCTIONS_DEFAULT_HINT}</p>
        <textarea
          className="form-control mb-2"
          rows={6}
          placeholder={saved[INSTRUCTIONS_KEY] || 'Using built-in default industry guidance (BFSI, Technology, Manufacturing, Healthcare, Real Estate).'}
          value={form[INSTRUCTIONS_KEY] ?? ''}
          onChange={set(INSTRUCTIONS_KEY)}
        />
        <button
          className="btn btn-sm btn-primary"
          onClick={() => save({ key: INSTRUCTIONS_KEY, label: 'AI instructions', secret: false })}
        >
          Save Instructions
        </button>
      </div>

      <div className="panel">
        {KEYS.map((item) => (
          <div className="row align-items-center mb-3" key={item.key}>
            <div className="col-md-4">
              <label className="form-label mb-0">{item.label}</label>
              {saved[item.key] && <div className="text-muted" style={{ fontSize: 12 }}>Current: {saved[item.key]}</div>}
            </div>
            <div className="col-md-6">
              <input
                type={item.secret ? 'password' : 'text'}
                className="form-control"
                placeholder={saved[item.key] ? 'Enter new value to rotate' : 'Not configured'}
                value={form[item.key] ?? ''}
                onChange={set(item.key)}
              />
            </div>
            <div className="col-md-2">
              <button className="btn btn-sm btn-outline-primary" onClick={() => save(item)}>Save</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
