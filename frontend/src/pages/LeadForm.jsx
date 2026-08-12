import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const SOURCES = ['LINKEDIN', 'REFERRAL', 'WEBSITE', 'EVENT', 'WEBINAR', 'COLD_OUTREACH', 'PREMIUM', 'OTHER'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function LeadForm() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '', email: '', phone: '', company: '', designation: '', industry: '',
    location: '', yearsExperience: '', linkedinUrl: '', source: 'LINKEDIN',
    priority: 'MEDIUM', notes: '', previousEngagementScore: 0,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setSaving(true); setError('');
    try {
      const payload = {
        ...form,
        yearsExperience: form.yearsExperience ? Number(form.yearsExperience) : null,
        previousEngagementScore: Number(form.previousEngagementScore) || 0,
      };
      const { data } = await api.post('/leads', payload);
      navigate('/leads/' + data.data.id);
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to create lead.');
    } finally {
      setSaving(false);
    }
  };

  const field = (label, key, type = 'text') => (
    <div className="col-md-6 mb-3">
      <label className="form-label">{label}</label>
      <input type={type} className="form-control" value={form[key]} onChange={set(key)} />
    </div>
  );

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">New Lead</h4>
      <div className="panel">
        {error && <div className="alert alert-danger py-2">{error}</div>}
        <div className="row">
          {field('Full Name *', 'name')}
          {field('Email', 'email')}
          {field('Phone', 'phone')}
          {field('Company', 'company')}
          {field('Designation', 'designation')}
          {field('Industry', 'industry')}
          {field('Location', 'location')}
          {field('Years of Experience', 'yearsExperience', 'number')}
          {field('LinkedIn URL', 'linkedinUrl')}
          {field('Previous Engagement Score', 'previousEngagementScore', 'number')}
          <div className="col-md-6 mb-3">
            <label className="form-label">Source</label>
            <select className="form-select" value={form.source} onChange={set('source')}>
              {SOURCES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="col-md-6 mb-3">
            <label className="form-label">Priority</label>
            <select className="form-select" value={form.priority} onChange={set('priority')}>
              {PRIORITIES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="col-12 mb-3">
            <label className="form-label">Notes</label>
            <textarea className="form-control" rows={3} value={form.notes} onChange={set('notes')} />
          </div>
        </div>
        <div className="d-flex gap-2">
          <button className="btn btn-brand" onClick={submit} disabled={saving || !form.name}>
            {saving ? 'Scoring with AI…' : 'Create & Score'}
          </button>
          <button className="btn btn-outline-secondary" onClick={() => navigate('/leads')}>Cancel</button>
        </div>
      </div>
    </div>
  );
}
