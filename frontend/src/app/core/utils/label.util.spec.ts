import { UserRole } from '../models/auth.models';
import { roleLabel } from './label.util';

describe('roleLabel', () => {
  it('maps AGENT_CREDIT technical role to Agent Crédit', () => {
    expect(roleLabel(UserRole.AGENT_CREDIT)).toBe('Agent Crédit');
    expect(roleLabel('AGENT_CREDIT')).toBe('Agent Crédit');
  });

  it('keeps other role labels unchanged', () => {
    expect(roleLabel(UserRole.RESPONSABLE_CREDIT)).toBe('Responsable Crédit');
    expect(roleLabel(UserRole.AUDITEUR)).toBe('Auditeur');
    expect(roleLabel(UserRole.ADMINISTRATEUR)).toBe('Administrateur');
  });

  it('renders migrated legacy roles under their replacement label', () => {
    expect(roleLabel(UserRole.UTILISATEUR)).toBe('Agent Crédit');
    expect(roleLabel(UserRole.VALIDATEUR)).toBe('Responsable Crédit');
  });
});
