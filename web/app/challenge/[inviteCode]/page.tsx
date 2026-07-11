import { FocusResetExperience } from "../../FocusResetExperience";

export default async function ChallengePage({
  params,
}: {
  params: Promise<{ inviteCode: string }>;
}) {
  const { inviteCode } = await params;
  return <FocusResetExperience inviteCode={inviteCode} />;
}
