export type ErrorCode =
  | "bridge_error"
  | "native_error"
  | "invalid_topic"
  | "token_unavailable";

export interface PluginError {
  code: ErrorCode;
  message: string;
  recoverable: boolean;
}

export type Failure = { status: "failed"; error: PluginError };
export type TokenResult =
  | { status: "success"; token: string }
  | { status: "unsupported" }
  | Failure;

export type PlatformResult =
  | { status: "success"; platform: string }
  | { status: "unsupported" }
  | Failure;

export type TopicResult =
  | { status: "subscribed"; topic: string }
  | { status: "unsubscribed"; topic: string }
  | { status: "unsupported" }
  | Failure;

export type PendingEvent =
  | { type: "token"; token: string }
  | {
      type: "message";
      platform: string;
      from?: string;
      messageId?: string;
      sentTimeMs: number;
      ttlSeconds: number;
      data: Record<string, string>;
      notificationTitle?: string;
      notificationBody?: string;
    };

export type PendingEventsResult =
  | { status: "success"; events: PendingEvent[] }
  | { status: "unsupported" }
  | Failure;

export function getToken(): Promise<TokenResult>;
export function getCurrentPlatform(): Promise<PlatformResult>;
export function subscribeToTopic(topic: string): Promise<TopicResult>;
export function unsubscribeFromTopic(topic: string): Promise<TopicResult>;
export function drainPendingEvents(): Promise<PendingEventsResult>;
