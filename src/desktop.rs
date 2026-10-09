use crate::{PendingEventsResult, PlatformResult, TokenResult, TopicResult};

pub(crate) fn get_token() -> TokenResult {
    TokenResult::Unsupported
}

pub(crate) fn get_current_platform() -> PlatformResult {
    PlatformResult::Unsupported
}

pub(crate) fn topic() -> TopicResult {
    TopicResult::Unsupported
}

pub(crate) fn drain_pending_events() -> PendingEventsResult {
    PendingEventsResult::Unsupported
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn desktop_surface_is_explicitly_unsupported() {
        assert_eq!(get_token(), TokenResult::Unsupported);
        assert_eq!(get_current_platform(), PlatformResult::Unsupported);
        assert_eq!(topic(), TopicResult::Unsupported);
        assert_eq!(drain_pending_events(), PendingEventsResult::Unsupported);
    }
}
