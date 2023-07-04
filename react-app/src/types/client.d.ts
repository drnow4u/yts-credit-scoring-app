export interface Client {
  name: string;
  language: string;
  additionalTextConsent: string | null;
  logo: string | null;
}
export interface ClientResponse extends Client {}
