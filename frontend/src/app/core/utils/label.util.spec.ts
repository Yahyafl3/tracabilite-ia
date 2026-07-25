import { UserRole } from '../models/auth.models';
import { roleLabel } from './label.util';

describe('roleLabel', () => {
  it('maps UTILISATEUR technical role to Agent de crédit', () => {
    expect(roleLabel(UserRole.UTILISATEUR)).toBe('Agent de crédit');
    expect(roleLabel('UTILISATEUR')).toBe('Agent de crédit');
  });

  it('keeps other role labels unchanged', () => {
    expect(roleLabel(UserRole.VALIDATEUR)).toBe('Validateur');
    expect(roleLabel(UserRole.AUDITEUR)).toBe('Auditeur');
    expect(roleLabel(UserRole.ADMINISTRATEUR)).toBe('Administrateur');
  });
});
