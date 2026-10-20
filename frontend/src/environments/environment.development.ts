// Configuracion para desarrollo local
import { environment as baseEnvironment } from './environment';

export const environment = {
  ...baseEnvironment,
  production: false,
  azure: {
    ...baseEnvironment.azure,
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200/login',
  },
  api: {
    ...baseEnvironment.api,
    baseUrl: 'http://localhost:8080/api',
  },
};
