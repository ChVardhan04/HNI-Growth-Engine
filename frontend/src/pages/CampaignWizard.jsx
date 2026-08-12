import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const STEPS = ['Basics', 'Targeting', 'Company Filters', 'Review & Launch'];

export default function CampaignWizard() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    name: '', type: 'LINKEDIN_PROSPECTING', audience: '',
    industry: '', designation: '', location: '',
    minExperience: '', maxExperience: '', keywords: '',
    companyName: '', companySize: '', minEmployeeCount: '', maxEmployeeCount: '',
    technologies: '', revenue: '', customSearchUrl: '',
  });

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const field = (label, key, type = 'text', placeholder = '') => (
    <div className="col-md-6 mb-3">
      <label className="form-label">{label}</label>
      <input type={type} className="form-control" value={form[key]} onChange={set(key)} placeholder={placeholder} />
    </div>
  );

  const next = () => setStep((s) => Math.min(s + 1, STEPS.length - 1));
  const back = () => setStep((s) => Math.max(s - 1, 0));

  const launch = async () => {
    setSaving(true); setError('');
    try {
      const payload = {
        ...form,
        minExperience: form.minExperience ? Number(form.minExperience) : null,
        maxExperience: form.maxExperience ? Number(form.maxExperience) : null,
        minEmployeeCount: form.minEmployeeCount ? Number(form.minEmployeeCount) : null,
        maxEmployeeCount: form.maxEmployeeCount ? Number(form.maxEmployeeCount) : null,
        keywords: form.keywords ? form.keywords.split(',').map((k) => k.trim()).filter(Boolean) : [],
        technologies: form.technologies ? form.technologies.split(',').map((k) => k.trim()).filter(Boolean) : [],
      };
      const { data } = await api.post('/campaigns', payload);
      const campaignId = data.data.id;
      await api.post(`/campaigns/${campaignId}/start`);
      navigate('/campaigns');
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to launch campaign.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">Campaign Wizard</h4>

      <div className="d-flex mb-4" style={{ gap: 8 }}>
        {STEPS.map((s, i) => (
          <div key={s} className={'badge-tier ' + (i === step ? 'tier-A' : 'tier-D')} style={{ cursor: 'pointer' }}
            onClick={() => setStep(i)}>
            {i + 1}. {s}
          </div>
        ))}
      </div>

      {error && <div className="alert alert-danger py-2">{error}</div>}

      <div className="panel">
        {step === 0 && (
          <div className="row">
            {field('Campaign Name *', 'name')}
            {field('Audience Description', 'audience', 'text', 'e.g. Tier A/A+ HNIs in metro regions')}
          </div>
        )}

        {step === 1 && (
          <div className="row">
            <p className="text-muted">These criteria are sent directly to the LinkedIn search - no manual import needed.</p>
            {field('Industry', 'industry', 'text', 'e.g. Finance, Technology')}
            {field('Designation', 'designation', 'text', 'e.g. Director, CFO, Founder')}
            {field('Location', 'location', 'text', 'e.g. Mumbai, Hyderabad')}
            {field('Min Experience (years)', 'minExperience', 'number')}
            {field('Max Experience (years)', 'maxExperience', 'number')}
            {field('Keywords (comma separated)', 'keywords', 'text', 'e.g. wealth management, portfolio')}
            <div className="col-12 mb-3">
              <label className="form-label">
                Custom LinkedIn Search URL <span className="text-muted">(optional, most reliable)</span>
              </label>
              <input type="text" className="form-control" value={form.customSearchUrl} onChange={set('customSearchUrl')}
                placeholder="Paste a URL copied from linkedin.com/search/results/people/ after setting filters there" />
              <div className="text-muted" style={{ fontSize: 12 }}>
                If set, this overrides all fields above - LinkedIn's own industry/location filters use exact
                values that are easy to get wrong when typed manually. Configure filters directly on LinkedIn,
                copy the resulting URL, and paste it here for guaranteed-accurate targeting.
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="row">
            {field('Company Name', 'companyName')}
            {field('Company Size', 'companySize', 'text', 'e.g. 201-500')}
            {field('Min Employee Count', 'minEmployeeCount', 'number')}
            {field('Max Employee Count', 'maxEmployeeCount', 'number')}
            {field('Technologies (comma separated)', 'technologies', 'text', 'e.g. Salesforce, SAP')}
            {field('Revenue (optional)', 'revenue', 'text', 'e.g. $10M-$50M')}
          </div>
        )}

        {step === 3 && (
          <div>
            <h6 className="mb-3">Review</h6>
            <table className="table-clean">
              <tbody>
                <tr><td>Name</td><td>{form.name || '-'}</td></tr>
                <tr><td>Industry</td><td>{form.industry || '-'}</td></tr>
                <tr><td>Designation</td><td>{form.designation || '-'}</td></tr>
                <tr><td>Location</td><td>{form.location || '-'}</td></tr>
                <tr><td>Experience</td><td>{form.minExperience || '-'} to {form.maxExperience || '-'} years</td></tr>
                <tr><td>Keywords</td><td>{form.keywords || '-'}</td></tr>
                <tr><td>Company</td><td>{form.companyName || '-'} ({form.companySize || 'any size'})</td></tr>
                <tr><td>Technologies</td><td>{form.technologies || '-'}</td></tr>
                <tr><td>Revenue</td><td>{form.revenue || '-'}</td></tr>
                <tr><td>Custom Search URL</td><td style={{ wordBreak: 'break-all' }}>{form.customSearchUrl || '-'}</td></tr>
              </tbody>
            </table>
            <p className="text-muted mt-2">
              Launching will automatically search LinkedIn, enrich results, store them as leads, and run AI ICP scoring - fully automated.
            </p>
          </div>
        )}

        <div className="d-flex justify-content-between mt-3">
          <button className="btn btn-outline-secondary" onClick={back} disabled={step === 0}>Back</button>
          {step < STEPS.length - 1 ? (
            <button className="btn btn-primary" onClick={next} disabled={step === 0 && !form.name}>Next</button>
          ) : (
            <button className="btn btn-primary" onClick={launch} disabled={saving || !form.name}>
              {saving ? 'Launching…' : 'Launch Campaign'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
